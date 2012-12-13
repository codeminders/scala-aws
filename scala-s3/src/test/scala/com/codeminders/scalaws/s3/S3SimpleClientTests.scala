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
import com.codeminders.scalaws.s3.model.Region
import scala.io.Source
import java.io.File

@RunWith(classOf[JUnitRunner])
class S3SimpleClientTests extends FunSuite {

  test("List Bucket Operation") {
    val client = AWSS3(AWSCredentials())
    val bucket = client.bucket("vorl2", Region.US_West).create()    
    bucket.key("1") <<< (new ByteArrayInputStream(Array[Byte]()), 0)
    bucket.key("2") <<< (new ByteArrayInputStream(Array[Byte]()), 0)
    bucket.key("3") <<< (new ByteArrayInputStream(Array[Byte]()), 0)
    assert(3 === bucket.keysNumber)
    assert(3 === bucket.foldLeft(0) { (r, e) => r + 1 })
    bucket.delete()
  }
  
//  test("Put Object Operation and Get Object Operation") {
//    val client = AWSS3(AWSCredentials(new FileInputStream("etc/AwsCredentials.properties")))
//    val bucket = client.bucket("s3index")
//    val key = bucket.key("1")
//    val data = "Data of Object 1"
//    key <<< (new ByteArrayInputStream(data.getBytes("UTF-8")), data.length())
//    val out = new ByteArrayOutputStream()
//    key >>> out
//    assert("Data of Object 1" === out.toString())
//  }
//  
//  test("HMAC Authentication. Set correct UID and Secret Key at etc/AwsCredentials.properties and run this test") {
//    val client = AWSS3(AWSCredentials(new FileInputStream("etc/AwsCredentials.properties")))
//    val bucket = client.bucket("s3index")
//    val key = bucket.key("1")
//    key.acl = "public-read"
//    val data = "Data of Object 1"
//    key <<< (new ByteArrayInputStream(data.getBytes("UTF-8")), data.length())
//    val out = new ByteArrayOutputStream()
//    key >>> out
//    assert("Data of Object 1" === out.toString())
//  }
//  
//  test("NoSuchBucket exception. Set correct UID and Secret Key at etc/AwsCredentials.properties and run this test") {
//    val client = AWSS3(AWSCredentials(new FileInputStream("etc/AwsCredentials.properties")))
//    val thrown = intercept[NoSuchBucketException] {
//      client.bucket("nosuchbucket").list().keysNumber
//    }
//    assert(thrown.statusCode === 404)
//    assert(thrown.errorCode === "NoSuchBucket")
//    assert(thrown.bucketName === "nosuchbucket")
//    assert(!thrown.hostId.isEmpty())
//  }

}


