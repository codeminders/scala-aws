package com.codeminders.scalaws.s3
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import scala.collection._
import http.HTTPClient
import org.scalatest.BeforeAndAfter
import scala.util.Random
import scala.collection.mutable.StringBuilder
import com.codeminders.scalaws.s3.model.Bucket

@RunWith(classOf[JUnitRunner])
abstract class BasicUnitTest extends FunSuite with BeforeAndAfter {

  private val bucketsToRemove = mutable.Set[Bucket]()
  private val bucketSymbolsRange = 'a'.toInt to 'z'.toInt

  implicit val client: AWSS3 = AWSS3(AWSCredentials())

  def removeBucketOnExit(bucket: Bucket) {
    bucketsToRemove.add(bucket)
  }
  
  def randomName(length: Int): String = {
    val result = new StringBuilder(length)
    (1 to length).foreach{
      i => 
       result.append((bucketSymbolsRange.start + (math.abs(Random.nextInt()) % bucketSymbolsRange.length)).toChar)
    }
    result.toString()
  }

  after {
    bucketsToRemove.foreach {
      bucket =>
        bucket.foreach {
          key =>
            key.delete()
        }
        bucket.delete()
    }
    bucketsToRemove.clear()
  }

}