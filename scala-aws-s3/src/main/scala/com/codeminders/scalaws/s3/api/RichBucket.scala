package com.codeminders.scalaws.s3.api

import scala.xml._
import java.io.InputStream
import java.io.ByteArrayInputStream
import scala.io.Source
import com.codeminders.scalaws.http.HTTPClient
import com.codeminders.scalaws.http.Response
import java.net.URL
import com.codeminders.scalaws.AmazonClientException
import com.codeminders.scalaws.s3.model.CannedACL
import com.codeminders.scalaws.s3.model.ExplicitACL
import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.s3.model.ObjectMetadata
import com.codeminders.scalaws.s3.model.ACL
import com.codeminders.scalaws.s3.model.Key
import com.codeminders.scalaws.s3.model.Region._
import sun.security.ssl.ByteBufferInputStream
import org.apache.commons.io.IOUtils
import java.util.Date
import com.codeminders.scalaws.utils.DateUtils
import com.codeminders.scalaws.s3.model.S3Object
import com.codeminders.scalaws.s3.model.S3ObjectBuilder
import com.codeminders.scalaws.s3.model.Expiration
import com.codeminders.scalaws.AmazonServiceException
import java.io.File
import java.io.FileInputStream
import com.codeminders.scalaws.s3.Request

class RichBucket(client: HTTPClient, val bucket: Bucket){
  
  def name = bucket.name
  
  def region = bucket.region
  
  def delete(key: Key): Unit = {
    client.delete(Request(bucket.name, key.name), (r: Response) => None)
  }
  
  def update(key: Key, s3ObjectBuilder: S3ObjectBuilder): RichS3Object = {
   val req = Request(name, key.name)
    client.put(req, (r: Response) => None)(s3ObjectBuilder.content, s3ObjectBuilder.contentLength)
    new RichS3Object(this.client, this.bucket, key)
  }
  
  def apply(key: Key): RichS3Object  = {
      new RichS3Object(this.client, this.bucket, key)
  }
  
  def list(prefix: String = "", delimiter: String = "", maxKeys: Int = 1000, marker: String = ""): Keys = {
    Keys(client, bucket, prefix, delimiter, maxKeys, marker)
  }
  
  def exist(key: Key): Boolean = {
    try{
	    client.head(Request(bucket.name, key.name))
	    true
    } catch {
      case e: AmazonServiceException => if(e.statusCode == 404) false else throw e
      case e => throw e
    }
  }
  
  override def toString() = bucket.toString()
  
}