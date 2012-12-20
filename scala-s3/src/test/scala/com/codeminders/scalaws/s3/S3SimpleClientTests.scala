package com.codeminders.scalaws.s3

import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import java.io.InputStream
import java.io.ByteArrayInputStream
import org.apache.commons.io.IOUtils
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import com.codeminders.scalaws.s3.model.CannedACL
import scala.io.Source
import scala.util.Random
import java.util.Date
import scala.collection.immutable.Map

@RunWith(classOf[JUnitRunner])
class S3SimpleClientTests extends BasicUnitTest {

  test("Verifies correctness of the List Bucket Operation") {
    val bucket = client.bucket("scala-aws-s3-test-list-bucket").create
    removeBucketOnExit(bucket)
    assert(0 === bucket.size)
    bucket.key("1").create
    bucket.key("2").create
    bucket.key("3").create
    assert(3 === bucket.size)
    assert(3 === bucket.foldLeft(0) { (r, e) => r + 1 })
  }
  
//  test("Verifies correctness of the Put Object Operation and the Get Object Operation") {
//    val bucket = client.bucket(randomName(16)).create
//    removeBucketOnExit(bucket)
//    val key = bucket.key("1")
//    val data = "Data of Object 1"
//    key <<< (new ByteArrayInputStream(data.getBytes("UTF-8")), data.length())
//    val out = new ByteArrayOutputStream()
//    key >>> out
//    assert("Data of Object 1" === out.toString())
//  }
//  
//  test("Get metadata of nonexistent object") {
//    val bucket = client.bucket(randomName(16)).create
//    removeBucketOnExit(bucket)
//    val key = bucket.key("1")
//    val thrown = intercept[NoSuchKeyException] {
//      key.metadata
//    }
//    assert(thrown.statusCode === 404)
//    assert(thrown.errorCode === "NoSuchKey")
//    assert(thrown.key === "1")
//    
//  }
//  
//  test("Checks default object's metadata values") {
//    val bucket = client.bucket(randomName(16)).create
//    removeBucketOnExit(bucket)
//    val objectNotCreated = new Date()
//    Thread.sleep(1000)
//    val key = bucket.key("1").create()
//    Thread.sleep(1000)
//    val objectCreated = new Date()
//    val metadata = key.metadata
//    assert(metadata.contentType === Some("application/octet-stream"))
//    assert(metadata.contentMD5 === Some("d41d8cd98f00b204e9800998ecf8427e"))
//    assert(metadata.size === Some(0))
//    assert(metadata.userMetadata === Map())
//    assert(metadata.expires === None)
//    assert(metadata.lastModified != None)
//    assert(metadata.lastModified.get.after(objectNotCreated))
//    assert(metadata.lastModified.get.before(objectCreated))
//  }
//  
//  test("Checks that NoSuchBucket exception is thrown for nonexistent bucket") {
//    val thrown = intercept[NoSuchBucketException] {
//      client.bucket("nosuchbucket").size
//    }
//    assert(thrown.statusCode === 404)
//    assert(thrown.errorCode === "NoSuchBucket")
//    assert(thrown.bucketName === "nosuchbucket")
//    assert(!thrown.hostId.isEmpty())
//  }

}


