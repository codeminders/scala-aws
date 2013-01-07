package com.codeminders.scalaws.s3

import com.codeminders.scalaws.http.HTTPClient
import com.codeminders.scalaws.http.ClientConfiguration
import com.codeminders.scalaws.http.HMACSingature
import com.codeminders.scalaws.http.ApacheHTTPClient
import com.codeminders.scalaws.AWSCredentials
import com.codeminders.scalaws.s3.model.Region._
import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.s3.model.Owner
import com.codeminders.scalaws.s3.api.RichBucket
import com.codeminders.scalaws.http.Response
import com.codeminders.scalaws.AmazonClientException
import com.codeminders.scalaws.AmazonServiceException
import java.net.URL
import java.io.ByteArrayInputStream
import scala.xml.XML

class AWSS3(config: ClientConfiguration) extends ApacheHTTPClient(config) with Traversable[RichBucket]{
  
  def delete(bucket: Bucket): Unit = {
    delete(Request(bucket.name), (r: Response) => None)
  }
  
  def create(bucket: Bucket, region: Region = US_Standard): RichBucket = {
    val req = Request(bucket.name)
    val data = region match {
      case US_Standard => {
        Array[Byte]()
      }
      case _ =>
        {
          (<CreateBucketConfiguration xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
             <LocationConstraint>{ region.toString() }</LocationConstraint>
           </CreateBucketConfiguration>).toString().getBytes()
        }
    }
    put(req, (r: Response) => None)(new ByteArrayInputStream(data), data.length)
    
    new RichBucket(this, bucket)
  }
  
  def apply(bucket: Bucket): RichBucket = {
    new RichBucket(this, bucket)
  }
  
  def exist(bucket: Bucket): Boolean = {
    try{
	    head(Request(bucket.name))
	    true
    } catch {
      case e: AmazonServiceException => if(e.statusCode == 404) false else throw e
      case e => throw e
    }
  }
  
  override def foreach[U](f: RichBucket =>  U): Unit = {
    getService()._1.foreach(f)
  }
  
  private def getService(): (Seq[RichBucket], Owner) = {

    def extractBucket(node: scala.xml.Node): RichBucket =
      node match {
        case <Bucket><Name>{ bucketName }</Name><CreationDate>{ creationDate }</CreationDate></Bucket> =>
          new RichBucket(this, new Bucket(bucketName text))
      }

    val responseHandler = (r: Response) => {
      r.content match {
        case None => throw AmazonClientException("Could not parse an empty response from server")
        case Some(is) => XML.load(is)
      }
    }

    val xml = get(Request(), responseHandler)

    ((xml \ "Bucket").foldLeft(Array[RichBucket]())((a, b) => a ++ Array(extractBucket(b))), new Owner(xml \ "Owner" \ "ID" text, xml \ "Owner" \ "DisplayName" text))
  }
  
}

object AWSS3 {
  def apply(cred: AWSCredentials): AWSS3 = {
    new AWSS3(new ClientConfiguration()) with HMACSingature { this.credentials = cred }
  }

  def apply(): AWSS3 = {
    new AWSS3(new ClientConfiguration())
  }

}