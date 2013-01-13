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

@RunWith(classOf[JUnitRunner])
class FunctionalTests extends BasicUnitTest {

  ignore("Verifies correctness of the List Bucket Operation") {
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

  ignore("Verifies correctness of the Put Object Operation and the Get Object Operation") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    bucket("1") = "Data of Object 1"
    assert("Data of Object 1" === IOUtils.toString(bucket("1").content))
  }

  ignore("Verifies correctness RichBucket's exist method") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    assert(client.exist(bucket))
    assert(!client.exist("no-such-bucket"))
  }

  ignore("Get metadata of nonexistent object") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    val thrown = intercept[AmazonServiceException] {
      bucket("1").metadata
    }
    assert(thrown.statusCode === 404)
  }

  ignore("Checks default object's metadata values") {
    val bucket = client.create(randomBucketName)
    removeBucketOnExit(bucket)
    val metadata = (bucket("1") = "1").metadata
    assert(metadata.contentType === Some("application/octet-stream"))
    assert(metadata.contentMD5 === Some("c4ca4238a0b923820dcc509a6f75849b"))
    assert(metadata.size === Some(1))
    assert(metadata.userMetadata === Map())
    assert(metadata.expiration === None)
    assert(metadata.lastModified != None)
    Thread.sleep(1000)
    assert(metadata.lastModified.get.after(DateUtils.parseRfc822Date("Sun, 1 Dec 2011 12:00:00 GMT")))
    assert(metadata.lastModified.get.before(new Date()))
  }

  ignore("Checks that NoSuchBucket exception is thrown for nonexistent bucket") {
    val thrown = intercept[NoSuchBucketException] {
      client("nosuchbucket")("1") = "1"
    }
    assert(thrown.statusCode === 404)
    assert(thrown.errorCode === "NoSuchBucket")
    assert(thrown.bucketName === "nosuchbucket")
    assert(!thrown.hostId.isEmpty())
  }

  ignore("Checks default object's acl values") {
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

  ignore("Verify that user can set object's ACL using cannedACL header") {
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

  ignore("Verify that user can set object's ACL using explicitACL headers") {
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
    bucket("1") = "data".withMetadata("k1", "v1").withMetadata("k2", "v2")
    val um = bucket("1").metadata.userMetadata
    assert(um.find(kv => ("k1", "v1") == kv) != None)
    assert(um.find(kv => ("k2", "v2") == kv) != None)
  }

}


