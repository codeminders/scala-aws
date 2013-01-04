package com.codeminders.scalaws.s3

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import org.scalatest.junit.JUnitRunner
import scala.collection._
import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.AWSCredentials


@RunWith(classOf[JUnitRunner])
abstract class BasicUnitTest extends FunSuite with BeforeAndAfter {
  
  private val bucketsToRemove = mutable.Set[Bucket]()
  private val bucketSymbolsRange = 'a'.toInt to 'z'.toInt

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

}