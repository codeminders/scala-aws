package com.codeminders.scalaws.s3.model

import scala.xml._
import java.io.InputStream
import java.io.ByteArrayInputStream
import scala.io.Source
import com.codeminders.scalaws.s3.http.HTTPClient
import com.codeminders.scalaws.s3.http.Response
import java.net.URL
import com.codeminders.scalaws.s3.http.Request
import CannedACL._
import sun.security.util.Length
import com.codeminders.scalaws.s3.AmazonClientException

object Region extends Enumeration {
  type Region = Value
  val US_Standard = Value("")
  val US_West = Value("us-west-1")
  val US_West_2 = Value("us-west-2")
  val EU_Ireland = Value("EU")
  val AP_Singapore = Value("ap-southeast-1")
  val AP_Tokyo = Value("ap-northeast-1")
  val SA_SaoPaulo = Value("sa-east-1")
}

import Region._

class RichBucket(val bucket: Bucket, prefix: String = "", delimiter: String = "/")(implicit val client: HTTPClient) extends KeysTree[RichBucket]{
  
  def name = bucket.name
  
  def region = bucket.region
  
  def key(name: String): RichKey = {
    new Key(bucket, name)
  }
  
  def create(): RichBucket = {
    create((r: Request) => {r})
  }
  
  def create(acl: ExplicitACL): RichBucket = {
    create((r: Request) => {
      acl.foreach(ea => ea._2.foreach(h => r.setHeader(h, ea._1.toString())))
      r
    })
  }
  
  def create(acl: CannedACL): RichBucket = {
    create((r: Request) => r.setHeader("x-amz-acl", acl.toString()))
  }
  
  def delete(): Unit = {
    client.delete(new Request(new URL("http://%s.s3.amazonaws.com".format(bucket.name))), (r: Response) => None)
  }
  
  def putBucketACL(acl: ACL, region: Region = US_Standard): Unit = {
    val req = new Request(new URL("http://%s.s3.amazonaws.com".format(bucket.name)))
//        client.put(req, (r: Response) => None)(new ByteArrayInputStream(data), data.length)
  }
  
  def list(prefix: String = "", delimiter: String = "/", maxKeys: Int = 1000, marker: String = ""): (Array[Key], Array[String]) = {

    def extractKey(node: scala.xml.Node): Key =
      node match {
        case <Contents><Key>{ name }</Key><LastModified>{ lastModified }</LastModified><ETag>{ etag }</ETag><Size>{ size }</Size><StorageClass>{ storageClass }</StorageClass></Contents> => 
          //new Key(bucket, name.text, lastModified.text, 0, etag.text, size.text.toInt, storageClass.text)
          new Key(bucket, name.text)
        case <Contents><Key>{ name }</Key><LastModified>{ lastModified }</LastModified><ETag>{ etag }</ETag><Size>{ size }</Size><Owner><ID>{ ownerId }</ID><DisplayName>{ ownerDisplayName }</DisplayName></Owner><StorageClass>{ storageClass }</StorageClass></Contents> => 
          //new Key(bucket, name.text, lastModified.text, 0, etag.text, size.text.toInt, storageClass.text)
          new Key(bucket, name.text)
      }
    
    val responseHandler = (r: Response) => {
      r.content match {
        case None => throw AmazonClientException("Could not parse an empty response from server")
        case Some(is) => XML.load(is) 
      }
    }
    
    val xml = client.get(new Request(new URL("http://%s.s3.amazonaws.com/?prefix=%s&delimiter=%s&max-keys=%d&marker=%s".format(bucket.name, prefix, delimiter, maxKeys, marker))), responseHandler)
    
    ((xml \ "Contents").foldLeft(Array[Key]())((a, b) => a ++ Array(extractKey(b))), (xml \ "CommonPrefixes" \ "Prefix").foldLeft(Array[String]())((a, b) => a ++ Array(b.text)))
  }
  
  override def toString() = bucket.toString()
  
  protected def newInstance(prefix: String, delimiter: String): RichBucket = {
    new RichBucket(bucket, prefix, delimiter)
  }
  
  private def create(applyACL: (Request) => Request): RichBucket = {
    val req = new Request(new URL("http://%s.s3.amazonaws.com".format(bucket.name)))
    val data = bucket.region match {
      case US_Standard => {
        Array[Byte]()
      }
      case _ =>
        {
          (<CreateBucketConfiguration xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
             <LocationConstraint>{ bucket.region.toString() }</LocationConstraint>
           </CreateBucketConfiguration>).toString().getBytes()
        }
    }
    applyACL(req)
    client.put(req, (r: Response) => None)(new ByteArrayInputStream(data), data.length)
    this
  }
  
}

class Bucket(val name: String, val region: Region = Region.US_Standard){
  override def toString() = "Bucket[%s(%s)]".format(name, region.toString())
}

object Bucket {
  implicit def bucket2RichBucket(b : Bucket)(implicit client: HTTPClient): RichBucket = {
	  new RichBucket(b)
  }
  
  implicit def richBucket2Bucket(b : RichBucket): Bucket = {
	  b.bucket
  }
}
