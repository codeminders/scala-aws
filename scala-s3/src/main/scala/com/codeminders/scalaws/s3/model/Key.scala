package com.codeminders.scalaws.s3.model

import java.io.InputStream
import java.io.OutputStream
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import scala.io.Source
import com.codeminders.scalaws.s3.http.HTTPClient
import com.codeminders.scalaws.s3.http.Request
import java.net.URL
import java.io.BufferedInputStream
import com.codeminders.scalaws.s3.http.Response

class Key(val client: HTTPClient, val bucket: Bucket, val name: String) {

  var acl: String = "private"

  var contentType: String = "binary/octet-stream"

  var expires: Long = 0

  var storageClass: String = "STANDARD"

  var objectSize: Long = 0

  var etag: String = ""

  var lastModified: String = ""

  var userMetadata: scala.collection.mutable.Map[String, String] = scala.collection.mutable.HashMap()

  var owner: Owner = new Owner("", "")
  
  def <<<(data: InputStream, length: Long) {
    create(data, length, (r: Request) => {r})
  }
  
  def <<<(data: InputStream, length: Long, acl: ExplicitACL) {
    create(data, length, (r: Request) => {
      acl.foreach(ea => ea._2.foreach(h => r.setHeader(h, ea._1.toString())))
      r
    })
  }
  
  def <<<(data: InputStream, length: Long, acl: CannedACL) {
    create(data, length, (r: Request) => r.setHeader("x-amz-acl", acl.toString()))
  }

  def >>>(out: OutputStream) {
    val req = new Request(new URL("http://s3.amazonaws.com/%s/%s".format(bucket.name, name)))
    client.get(req, (r: Response) => {
      r.content match {
        case None =>
        case Some(is) => IOUtils.copy(is, out) 
      }
    })
  }

  override def toString() = "contentType:%s, expires:%d, storageClass:%s, size:%d, etag:%s, lastModified:%s, userMetadata:[%s]".format(contentType, expires, storageClass, objectSize, etag, lastModified, userMetadata.mkString(", "))

  def withContentType(contentType: String): Key = {
    this.contentType = contentType
    this
  }

  def withExpires(expires: Long): Key = {
    this.expires = expires
    this
  }

  def withStorageClass(storageClass: String): Key = {
    this.storageClass = storageClass
    this
  }

  def withSize(size: Long): Key = {
    this.objectSize = size
    this
  }

  def withEtag(etag: String): Key = {
    this.etag = etag
    this
  }

  def withLastModified(lastModified: String): Key = {
    this.lastModified = lastModified
    this
  }

  def withOwner(owner: Owner): Key = {
    this.owner = owner
    this
  }

  def withACL(acl: String): Key = {
    this.acl = acl
    this
  }
  
  private def create(data: InputStream, length: Long, applyACL: (Request) => Request): Key = {
    val req = new Request(new URL("http://s3.amazonaws.com/%s/%s".format(bucket.name, name)))
    applyACL(req)
    client.put(req, (r: Response) => None)(data, length)
    this
  }
}

object Key {
  def apply(client: HTTPClient, bucket: Bucket, name: String):Key = new Key(client, bucket, name)
  
  def apply(client: HTTPClient, bucket: Bucket, name: String, lastModified:String, etag: String, size: Long, storageClass: String = "STANDARD", owner: Owner = new Owner("", "")):Key = 
    new Key(client, bucket, name).withLastModified(lastModified).withEtag(etag).withSize(size).withStorageClass(storageClass).withOwner(owner)
}