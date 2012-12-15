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
import com.codeminders.scalaws.s3.model.Key._
import scala.io.Source
import com.codeminders.scalaws.s3.model.RichKey
import com.codeminders.scalaws.s3.model.Key
import scala.util.Random

@RunWith(classOf[JUnitRunner])
class S3SimpleClientTests extends BasicUnitTest {

  test("List Bucket Operation") {
    val bucket = client.bucket(randomName(16)).create
    removeBucketOnExit(bucket)
    assert(0 === bucket.keysNumber)
    bucket.key("1") <<< (new ByteArrayInputStream(Array[Byte]()), 0)
    bucket.key("2") <<< (new ByteArrayInputStream(Array[Byte]()), 0)
    bucket.key("3") <<< (new ByteArrayInputStream(Array[Byte]()), 0)
    assert(3 === bucket.refresh.keysNumber)
    assert(3 === bucket.refresh.foldLeft(0) { (r, e) => r + 1 })
  }
  
  test("Put Object Operation and Get Object Operation") {
    val bucket = client.bucket(randomName(16)).create
    removeBucketOnExit(bucket)
    val key = bucket.key("1")
    val data = "Data of Object 1"
    key <<< (new ByteArrayInputStream(data.getBytes("UTF-8")), data.length())
    val out = new ByteArrayOutputStream()
    key >>> out
    assert("Data of Object 1" === out.toString())
  }
  
  test("NoSuchBucket exception") {
    val thrown = intercept[NoSuchBucketException] {
      client.bucket("nosuchbucket").keysNumber
    }
    assert(thrown.statusCode === 404)
    assert(thrown.errorCode === "NoSuchBucket")
    assert(thrown.bucketName === "nosuchbucket")
    assert(!thrown.hostId.isEmpty())
  }

}


