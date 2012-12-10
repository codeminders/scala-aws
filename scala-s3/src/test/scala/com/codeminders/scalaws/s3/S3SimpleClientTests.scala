package com.codeminders.scalaws.s3

import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import java.io.InputStream
import java.io.ByteArrayInputStream
import org.apache.commons.io.IOUtils
import java.io.ByteArrayOutputStream
import java.io.FileInputStream

@RunWith(classOf[JUnitRunner])
class S3SimpleClientTests extends FunSuite {

//  test("List Bucket Operation") {
//    val client = new SimpleS3() with HTTPClientMock
//    val bucket = client.bucket("s3index")
//    val tree = bucket.list()
//    assert(1 === tree.keysNumber)
//    assert(3 === tree.groupsNumber)
//    assert(6 === tree.foldLeft(0) { (r, e) => r + 1 })
//  }
//  
//  test("Put Object Operation and Get Object Operation") {
//    val client = new SimpleS3() with HTTPClientMock
//    val bucket = client.bucket("s3index")
//    val key = bucket.key("1")
//    val data = "Data of Object 1"
//    key <<< (new ByteArrayInputStream(data.getBytes("UTF-8")), data.length())
//    val out = new ByteArrayOutputStream()
//    key >>> out
//    assert("Data of Object 1" === out.toString())
//  }
  
  test("HMAC Authentication. Set correct UID and Secret Key at etc/AwsCredentials.properties and run this test") {
    val client = AWSS3(AWSCredentials(new FileInputStream("etc/AwsCredentials.properties")))
    val bucket = client.bucket("s3index")
    val key = bucket.key("1")
    key.acl = "public-read"
    val data = "Data of Object 1"
    key <<< (new ByteArrayInputStream(data.getBytes("UTF-8")), data.length())
    val out = new ByteArrayOutputStream()
    key >>> out
    assert("Data of Object 1" === out.toString())
  }
  
  test("NoSuchBucket exception. Set correct UID and Secret Key at etc/AwsCredentials.properties and run this test") {
    val client = AWSS3(AWSCredentials(new FileInputStream("etc/AwsCredentials.properties")))
    val thrown = intercept[NoSuchBucketException] {
      client.bucket("nosuchbucket").list().keysNumber
    }
    assert(thrown.statusCode === 404)
    assert(thrown.errorCode === "NoSuchBucket")
    assert(thrown.bucketName === "nosuchbucket")
    assert(!thrown.hostId.isEmpty())
  }

}


