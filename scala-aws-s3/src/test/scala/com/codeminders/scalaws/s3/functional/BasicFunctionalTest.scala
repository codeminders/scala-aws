package com.codeminders.scalaws.s3.functional

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import org.scalatest.junit.JUnitRunner
import scala.collection._
import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.AWSCredentials
import java.util.Random
import com.codeminders.scalaws.s3.AWSS3
import com.codeminders.scalaws.s3.Implicits._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
abstract class BasicUnitTest extends FunSuite with BeforeAndAfter {
  
  private val bucketsToRemove = mutable.Set[Bucket]()
  private val asciiSymbolsRange = 'a' to 'z'
  private val numbersRange = '0' to '9'
  private val random = new Random()

  var client: AWSS3 = AWSS3(AWSCredentials())

  after {
    bucketsToRemove.foreach {
      bucket =>
        client(bucket).list().foreach {
          key =>
            client(bucket).delete(key)
        }
        client.delete(bucket.name)
    }
    bucketsToRemove.clear()
  }

  def removeBucketOnExit(bucket: Bucket) {
    bucketsToRemove.add(bucket)
  }
  
  def randomBucketName: String = {
    val res = "scala-aws-" + (0 to 8).foldLeft(""){
      (s, i) => 
        val r = math.abs(random.nextInt() % 8)
        r % 2 match {
          case 0 => s + asciiSymbolsRange(r)
          case 1 => s + numbersRange(r)
        }
    } 
    res
  }

}