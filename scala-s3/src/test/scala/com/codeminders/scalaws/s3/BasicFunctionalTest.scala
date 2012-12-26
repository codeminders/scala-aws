package com.codeminders.scalaws.s3
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest._
import scala.collection._
import http.HTTPClient
import org.scalatest.BeforeAndAfter
import scala.util.Random
import scala.collection.mutable.StringBuilder
import com.codeminders.scalaws.s3.model.Bucket
import http.ClientConfiguration
import http.HMACSingature
import com.codeminders.scalaws.s3.http.HTTPClientCache
import scala.collection.immutable.Map

@RunWith(classOf[JUnitRunner])
abstract class BasicUnitTest extends FunSuite with BeforeAndAfter {

  private val bucketsToRemove = mutable.Set[Bucket]()
  private val bucketSymbolsRange = 'a'.toInt to 'z'.toInt
  
  implicit var client: AWSS3 = null
  
  override protected def runTest(testName: String, reporter: Reporter, stopper: Stopper, configMap: Map[String, Any], tracker: Tracker) {
    client = new AWSS3(new ClientConfiguration()) with HMACSingature with HTTPClientCache { 
      override val cacheId = testName
      override val credentials = AWSCredentials() 
    }
    super.runTest(testName, reporter, stopper, configMap, tracker)
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
  
  def removeBucketOnExit(bucket: Bucket) {
    bucketsToRemove.add(bucket)
  }
  
}