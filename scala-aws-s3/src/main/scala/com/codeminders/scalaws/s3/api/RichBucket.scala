package com.codeminders.scalaws.s3.api

import scala.xml._
import java.io.InputStream
import java.io.ByteArrayInputStream
import scala.io.Source
import com.codeminders.scalaws.http.HTTPClient
import com.codeminders.scalaws.http.Response
import java.net.URL
import com.codeminders.scalaws.AmazonClientException
import com.codeminders.scalaws.s3.model.CannedACL._
import com.codeminders.scalaws.s3.model.Permission._
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
  
  def update(key: Key, objectBuilder: S3ObjectBuilder): RichS3Object = {
   val req = Request(name, key.name, headers = objectBuilder.metadata)
    client.put(req, (r: Response) => None)(objectBuilder.content, objectBuilder.contentLength)
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
  
  def acl: ACL = {
    
    val req = Request(bucket.name, parameters=Array(("acl", "")))
    ACL(client.get(req, (r: Response) => {
      r.content match {
        case None => throw AmazonClientException("Could not parse an empty response from server")
        case Some(is) => XML.load(is)
      }
    }))
  }
  
  def acl_=(newACL: ACL) = {
    val r = Request(bucket.name, parameters=Array(("acl", "")))
    val data = newACL.toXML.buildString(true)
    client.put(r, (r: Response) => None)(IOUtils.toInputStream(data), data.length())
  }
  
  def acl_=(newACL: CannedACL) = {
    val r = Request(bucket.name, parameters=Array(("acl", "")), headers=Array(("x-amz-acl", newACL.toString())))
    client.put(r, (r: Response) => None)(IOUtils.toInputStream(""), 0)
  }
  
  def acl_=(newACL: Map[Permission, Seq[String]]) = {
    require(!newACL.isEmpty, "Could not set ACL from empty value")
    val aclHeaders = newACL.foldLeft(Array.empty[(String, String)]){
    	(a, e) =>
        e._1 match {
          case READ => a ++ e._2.foldLeft(Array.empty[(String, String)])((arr, el) => arr :+ Tuple2("x-amz-grant-read", el))
          case WRITE => a ++ e._2.foldLeft(Array.empty[(String, String)])((arr, el) => arr :+ Tuple2("x-amz-grant-write", el))
          case READ_ACP => a ++ e._2.foldLeft(Array.empty[(String, String)])((arr, el) => arr :+ Tuple2("x-amz-grant-read-acp", el))
          case WRITE_ACP => a ++ e._2.foldLeft(Array.empty[(String, String)])((arr, el) => arr :+ Tuple2("x-amz-grant-write-acp", el))
          case FULL_CONTROL => a ++ e._2.foldLeft(Array.empty[(String, String)])((arr, el) => arr :+ Tuple2("x-amz-grant-full-control", el))
        }
      }
    val r = Request(bucket.name, parameters=Array(("acl", "")), headers=aclHeaders)
    client.put(r, (r: Response) => None)(IOUtils.toInputStream(""), 0)
  }
  
  override def toString() = bucket.toString()
  
}