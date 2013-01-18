package com.codeminders.scalaws.s3.functional

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.apache.commons.io.IOUtils
import java.util.Date
import scala.collection.immutable.Map
import com.codeminders.scalaws.AmazonServiceException
import com.codeminders.scalaws.utils.DateUtils
import com.codeminders.scalaws.NoSuchBucketException
import com.codeminders.scalaws.s3.model.CanonicalGrantee
import com.codeminders.scalaws.s3.Implicits._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.codeminders.scalaws.s3.model.CannedACL
import com.codeminders.scalaws.s3.model.Permission
import com.codeminders.scalaws.s3.model.GroupGrantee
import com.codeminders.scalaws.s3.model.Grant
import com.codeminders.scalaws.s3.model.Grantee
import com.codeminders.scalaws.s3.model.ACL
import com.codeminders.scalaws.s3.model.CanonicalGrantee
import com.codeminders.scalaws.s3.model.Key
import com.codeminders.scalaws.s3.model.S3Object
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.File
import java.io.ByteArrayInputStream
import com.codeminders.scalaws.s3.model.MultipartUploadBuilder
import com.codeminders.scalaws.s3.api.MultipartUploadOutputStream
import com.codeminders.scalaws.ConsistentMockInputStream
import scala.collection.mutable.ArrayBuffer
import com.codeminders.scalaws.s3.model.S3ObjectBuilder
import com.codeminders.scalaws.utils.Utils
import com.codeminders.scalaws.s3.model.StorageClass

@RunWith(classOf[JUnitRunner])
class FunctionalTests extends BasicUnitTest {

  test("Verifies correctness of the List Bucket Operation") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    assert(0 === bucket.list().size)
    bucket("1") = "1"
    bucket("A/2") = "2"
    bucket("A/3") = "3"
    bucket("B/4") = "4"
    bucket("B/5") = "5"
    bucket("B/6") = "6"
    assert(0 === bucket.list().commonPrefexes.size)
    assert(6 === bucket.list().length)
    assert(0 === bucket.list(maxKeys = 1).commonPrefexes.size)
    assert(6 === bucket.list(maxKeys = 1).length)
    assert(2 === bucket.list(delimiter = "/").commonPrefexes.size)
    assert(1 === bucket.list(delimiter = "/").length)
    assert("A/" === bucket.list(delimiter = "/").commonPrefexes(0).prefix)
    assert(2 === bucket.list(delimiter = "/").commonPrefexes(0).length)
    assert("B/" === bucket.list(delimiter = "/").commonPrefexes(1).prefix)
    assert(3 === bucket.list(delimiter = "/").commonPrefexes(1).length)
  }

  test("Verifies correctness of the Put Object Operation and the Get Object Operation") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    bucket("1") = "Data of Object 1"
    assert("Data of Object 1" === IOUtils.toString(bucket("1").inputStream()))
  }

  test("Checks that one can read a range of bytes of an object") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    bucket("1") = "Data of Object 1"
    assert("Object 1" === IOUtils.toString(bucket("1").inputStream(8)))
    val content = bucket("1").content(5, 2)
    assert("of" === IOUtils.toString(content._1))
    assert(2 === content._2)
    assert(9 === content._3)
  }

  test("Verifies correctness RichBucket's exist method") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    assert(client.exist(bucket))
    assert(!client.exist("no-such-bucket"))
  }

  test("Get metadata of nonexistent object") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    val thrown = intercept[AmazonServiceException] {
      bucket("1").metadata
    }
    assert(thrown.statusCode === 404)
  }

  test("Checks default object's metadata values") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    val metadata = (bucket("1") = "1").metadata
    assert(metadata.contentType === Some("application/octet-stream"))
    assert(metadata.contentMD5 === Some("c4ca4238a0b923820dcc509a6f75849b"))
    assert(metadata.size === Some(1))
    assert(metadata.userMetadata === Seq.empty[(String, String)])
    assert(metadata.expiration === None)
    assert(metadata.lastModified != None)
    Thread.sleep(1000)
    assert(metadata.lastModified.get.after(DateUtils.parseRfc822Date("Sun, 1 Dec 2011 12:00:00 GMT")))
    assert(metadata.lastModified.get.before(new Date()))
  }

  test("Checks that NoSuchBucket exception is thrown for nonexistent bucket") {
    val thrown = intercept[NoSuchBucketException] {
      client("nosuchbucket")("1") = "1"
    }
    assert(thrown.statusCode === 404)
    assert(thrown.errorCode === "NoSuchBucket")
    assert(thrown.bucketName === "nosuchbucket")
    assert(!thrown.hostId.isEmpty())
  }

  test("Checks object's default ACL") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    bucket("1") = "Data of Object 1"
    val acl = bucket("1").acl
    assert(!acl.owner.uid.isEmpty())
    assert(!acl.owner.displayName.isEmpty())
    assert(acl.grants.size === 1)
    assert(acl.grants(0).grantee.isInstanceOf[CanonicalGrantee])
    assert(acl.grants(0).grantee.asInstanceOf[CanonicalGrantee].uid === acl.owner.uid)
    assert(acl.grants(0).grantee.asInstanceOf[CanonicalGrantee].displayName === acl.owner.displayName)
  }

  test("Verify that user can set object's ACL using cannedACL header on object creation") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    bucket("1") = "data".withCannedACL(CannedACL.PublicRead)
    val acl = bucket("1").acl
    assert(!acl.owner.uid.isEmpty())
    assert(!acl.owner.displayName.isEmpty())
    assert(acl.grants.size === 2)
    assert(acl.grants.find(g => g == new Grant(GroupGrantee.AllUsers, Permission.READ)) != None)
    assert(acl.grants.find(g => g == new Grant(new CanonicalGrantee(acl.owner.uid, acl.owner.displayName), Permission.FULL_CONTROL)) != None)
  }

  test("Verify that user can set object's ACL using explicitACL headers on object creation") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    bucket("1") = "data".withExplicitACL(Permission.READ_ACP, GroupGrantee.AllUsers.granteeID).withExplicitACL(Permission.WRITE_ACP, GroupGrantee.AllUsers.granteeID)
    val acl = bucket("1").acl
    assert(!acl.owner.uid.isEmpty())
    assert(!acl.owner.displayName.isEmpty())
    assert(acl.grants.size === 2)
    assert(acl.grants.find(g => g == new Grant(GroupGrantee.AllUsers, Permission.READ_ACP)) != None)
    assert(acl.grants.find(g => g == new Grant(GroupGrantee.AllUsers, Permission.WRITE_ACP)) != None)
  }

  test("Verify that user can set object's metadata on object creation") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    bucket("1") = "data".withMetadata("k1", "v1").withMetadata("k2", "v2").withMetadata("k2", "v3")
    val um = bucket("1").metadata.userMetadata
    assert(um.find(kv => ("k1", "v1") == kv) != None)
    assert(um.find(kv => ("k2", "v3,v2") == kv) != None)
  }

  test("Verify PUT Object ACL functionality") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    bucket("1") = "data"
    //prepare ACL
    val defaultACL = bucket("1").acl
    assert(!defaultACL.owner.uid.isEmpty())
    assert(!defaultACL.owner.displayName.isEmpty())
    val otherHasReadACL = new Grant(GroupGrantee.AllUsers, Permission.READ_ACP)
    val otherHasWriteACL = new Grant(GroupGrantee.AllUsers, Permission.WRITE_ACP)
    val ownerHasFullControl = new Grant(new CanonicalGrantee(defaultACL.owner.uid, defaultACL.owner.displayName), Permission.FULL_CONTROL)
    //set ACL
    bucket("1").acl = new ACL(defaultACL.owner, List(otherHasReadACL, otherHasWriteACL, ownerHasFullControl))
    //check whether ACL has been updated
    val acl = bucket("1").acl
    assert(!acl.owner.uid.isEmpty())
    assert(!acl.owner.displayName.isEmpty())
    assert(acl.grants.size === 3)
    assert(acl.grants.find(g => g == otherHasReadACL) != None)
    assert(acl.grants.find(g => g == otherHasWriteACL) != None)
    assert(acl.grants.find(g => g == ownerHasFullControl) != None)
  }

  test("Verify PUT Object ACL functionality (Canned ACL)") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    bucket("1") = "data"
    bucket("1").acl = CannedACL.PublicRead
    val acl = bucket("1").acl
    assert(!acl.owner.uid.isEmpty())
    assert(!acl.owner.displayName.isEmpty())
    assert(acl.grants.size === 2)
    assert(acl.grants.find(g => g == new Grant(GroupGrantee.AllUsers, Permission.READ)) != None)
    assert(acl.grants.find(g => g == new Grant(new CanonicalGrantee(acl.owner.uid, acl.owner.displayName), Permission.FULL_CONTROL)) != None)
  }

  test("Verify PUT Object ACL functionality (Explicit ACL)") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    bucket("1") = "data"
    bucket("1").acl = Map(Permission.READ_ACP -> List(GroupGrantee.AllUsers.granteeID), Permission.WRITE_ACP -> List(GroupGrantee.AllUsers.granteeID))
    val acl = bucket("1").acl
    assert(!acl.owner.uid.isEmpty())
    assert(!acl.owner.displayName.isEmpty())
    assert(acl.grants.size === 2)
    assert(acl.grants.find(g => g == new Grant(GroupGrantee.AllUsers, Permission.READ_ACP)) != None)
    assert(acl.grants.find(g => g == new Grant(GroupGrantee.AllUsers, Permission.WRITE_ACP)) != None)
  }

  test("Checks bucket's default ACL") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    val acl = bucket.acl
    assert(!acl.owner.uid.isEmpty())
    assert(!acl.owner.displayName.isEmpty())
    assert(acl.grants.size === 1)
    assert(acl.grants(0).grantee.isInstanceOf[CanonicalGrantee])
    assert(acl.grants(0).grantee.asInstanceOf[CanonicalGrantee].uid === acl.owner.uid)
    assert(acl.grants(0).grantee.asInstanceOf[CanonicalGrantee].displayName === acl.owner.displayName)
  }

  test("Verify PUT Bucket ACL functionality") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    //prepare ACL
    val defaultACL = bucket.acl
    assert(!defaultACL.owner.uid.isEmpty())
    assert(!defaultACL.owner.displayName.isEmpty())
    val otherHasReadACL = new Grant(GroupGrantee.AllUsers, Permission.READ_ACP)
    val otherHasWriteACL = new Grant(GroupGrantee.AllUsers, Permission.WRITE_ACP)
    val ownerHasFullControl = new Grant(new CanonicalGrantee(defaultACL.owner.uid, defaultACL.owner.displayName), Permission.FULL_CONTROL)
    //set ACL
    bucket.acl = new ACL(defaultACL.owner, List(otherHasReadACL, otherHasWriteACL, ownerHasFullControl))
    //check whether ACL has been updated
    val acl = bucket.acl
    assert(!acl.owner.uid.isEmpty())
    assert(!acl.owner.displayName.isEmpty())
    assert(acl.grants.size === 3)
    assert(acl.grants.find(g => g == otherHasReadACL) != None)
    assert(acl.grants.find(g => g == otherHasWriteACL) != None)
    assert(acl.grants.find(g => g == ownerHasFullControl) != None)
  }

  test("Verify PUT Bucket ACL functionality (Canned ACL)") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    bucket.acl = CannedACL.PublicRead
    val acl = bucket.acl
    assert(!acl.owner.uid.isEmpty())
    assert(!acl.owner.displayName.isEmpty())
    assert(acl.grants.size === 2)
    assert(acl.grants.find(g => g == new Grant(GroupGrantee.AllUsers, Permission.READ)) != None)
    assert(acl.grants.find(g => g == new Grant(new CanonicalGrantee(acl.owner.uid, acl.owner.displayName), Permission.FULL_CONTROL)) != None)
  }

  test("Verify PUT Bucket ACL functionality (Explicit ACL)") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    //prepare ACL
    val defaultACL = bucket.acl
    bucket.acl = Map(
      Permission.READ_ACP -> List(GroupGrantee.AllUsers.granteeID),
      Permission.WRITE_ACP -> List(GroupGrantee.AllUsers.granteeID),
      Permission.FULL_CONTROL -> List("""id="%s"""".format(defaultACL.owner.uid)))
    val acl = bucket.acl
    assert(!acl.owner.uid.isEmpty())
    assert(!acl.owner.displayName.isEmpty())
    assert(acl.grants.size === 3)
    assert(acl.grants.find(g => g == new Grant(GroupGrantee.AllUsers, Permission.READ_ACP)) != None)
    assert(acl.grants.find(g => g == new Grant(GroupGrantee.AllUsers, Permission.WRITE_ACP)) != None)
    assert(acl.grants.find(g => g == new Grant(new CanonicalGrantee(acl.owner.uid, acl.owner.displayName), Permission.FULL_CONTROL)) != None)
  }

  test("Verifies correctness of the Copy Object Operation") {
    val bucket1 = client.create(randomBucketName)
    val bucket2 = client.create(randomBucketName)
    removeBucketOnExit(bucket1)
    removeBucketOnExit(bucket2)
    bucket2("2") = bucket1("1") = "Data of Object 1"
    assert("Data of Object 1" === IOUtils.toString(bucket2("2").inputStream))
  }

  test("Verifies MultipartUploadOutputStream") {
    val mb = 1024 * 1024
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    Utils.using(new MultipartUploadOutputStream(bucket.initiateUpload("1"))) {
      outputStream =>
        IOUtils.copy(new ConsistentMockInputStream(0, 6 * mb), outputStream)
    }
    assertInputStreamsEqual(new ConsistentMockInputStream(0, 6 * mb), bucket("1").inputStream)
  }

  test("Upload an InputStream using Multipart Upload") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    bucket("1") = new ByteArrayInputStream("Data of Object 1".getBytes())
    assert("Data of Object 1" === IOUtils.toString(bucket("1").inputStream()))
  }

  test("Creates an object from input stream and other object using Multipart Upload") {
    val mb = 1024 * 1024
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    val srcObj = bucket("src") = (new ConsistentMockInputStream(0, 6 * mb), 6 * mb)
    val dstObj = Utils.using(bucket.initiateUpload("dst")) {
      upload =>
        upload(1) = (new ConsistentMockInputStream(0, 6 * mb), 6 * mb)
        upload(2) = srcObj
    }
    assertInputStreamsEqual(new ConsistentMockInputStream(0, 12 * mb), dstObj.inputStream)
  }

  test("Initiate Multipart Upload, upload parts and list parts") {
    val testStartTS = new Date()
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    Thread.sleep(1000)
    val upload = bucket.initiateUpload("obj")
    try {
      upload(1) = (new ConsistentMockInputStream(0, 1024), 1024)
      upload(2) = (new ConsistentMockInputStream(0, 1024), 1024)
      upload(3) = (new ConsistentMockInputStream(0, 1024), 1024)
      upload(4) = (new ConsistentMockInputStream(0, 1024), 1024)
      upload(5) = (new ConsistentMockInputStream(0, 1024), 1024)
      val parts = upload.list(2).toArray
      assert(parts.size === 5)
      parts.zipWithIndex.foreach(p => assert(p._1.partNumber === p._2 + 1))
      parts.foreach(p => assert(p.size === 1024))
      parts.foreach(p => assert(p.etag === "b2ea9f7fcea831a4a63b213f41a8855b"))
      parts.foreach(p => assert(p.lastModified.after(testStartTS)))
    } finally {
      upload.abort
    }
  }
  
  test("Verify List Multipart Uploads operation") {
    val testStartTS = new Date()
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    Thread.sleep(1000)
    val bucketOwner = bucket.owner
    val upload1 = bucket.initiateUpload("obj2")
    val upload2 = bucket.initiateUpload("obj1")
    val upload3 = bucket.initiateUpload("obj2")
    val upload4 = bucket.initiateUpload("obj1")
    val upload5 = bucket.initiateUpload("obj2")
    val upload6 = bucket.initiateUpload("obj3")
    try {
      val uploads = bucket.listUploads(maxUploads=2).toSeq
      assert(uploads.size === 6)
      uploads.foreach(u => assert(u.storageClass === StorageClass.STANDARD))
      uploads.foreach(u => assert(u.owner === bucketOwner))
      uploads.foreach(u => assert(u.initiator === bucketOwner))
      uploads.foreach(u => assert(u.initiated.after(testStartTS)))
    } finally {
      upload1.abort
      upload2.abort
      upload3.abort
      upload4.abort
      upload5.abort
      upload6.abort
    }
  }

}


