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
import scala.collection.immutable.Map
import scala.collection._
import com.codeminders.scalaws.s3.AmazonClientException
import java.util.Date
import com.codeminders.scalaws.s3.DateUtils

object StorageClass extends Enumeration {
  type StorageClass = Value
  val STANDARD, REDUCED_REDUNDANCY = Value
}

import StorageClass._

class RichKey(client: HTTPClient, key: Key) {

  private val CONTENT_TYPE = "Content-Type"
  private val EXPIRATION = "x-amz-expiration"
  private val OBJECT_SIZE = "Content-Length"
  private val ETAG = "ETag"
  private val CONTENT_MD5 = "Content-MD5"
  private val DATE = "Date"
  private val X_AMZ_META = "x-amz-meta-"

  lazy val keyMetadata = {
    val m = mutable.Map(head.toList: _*)
    def addIfNotNone[T](k: Option[T], transformer: (T) => String) = {
      k match {
        case None =>
        case Some(v) => m += (CONTENT_TYPE -> v.toString())
      }
    }
    addIfNotNone[String](key.contentType, (o) => o.toString())
    addIfNotNone[Long](key.expires, (o) => o.toString())
    addIfNotNone[Long](key.size, (o) => o.toString())
    addIfNotNone[String](key.contentMD5, (o) => o.toString())
    addIfNotNone[Date](key.lastModified, (o) => DateUtils.formatRfc822Date(o))
    key.userMetadata.foreach(kv => m += (X_AMZ_META + kv._1 -> kv._2))
    m
  }

  lazy val bucket = key.bucket

  lazy val name = key.name

  def contentType = keyMetadata.get(CONTENT_TYPE)

  def contentType_=(value: String) = {
    keyMetadata(CONTENT_TYPE) = value
  }

  def expires = keyMetadata.get(EXPIRATION) match {
    case None => None
    case Some(v) => Option(v.toLong)
  }

  def expires_=(value: Long) = {
    keyMetadata(EXPIRATION) = value.toString()
  }

  def size = keyMetadata.get(EXPIRATION) match {
    case None => None
    case Some(v) => Option(v.toLong)
  }

  def length = size

  def contentMD5 = keyMetadata.get(CONTENT_TYPE)

  def contentMD5_=(value: String) = {
    keyMetadata(CONTENT_MD5) = value
  }

  def lastModified = keyMetadata.get(EXPIRATION) match {
    case None => None
    case Some(v) => Option(DateUtils.parseRfc822Date(v))
  }

  def userMetadata: Map[String, String] = {
    Map(keyMetadata.filter(e => e._1.startsWith(X_AMZ_META)).toList: _*)
  }

  def userMetadata_=(kv: (String, String)) = {
    keyMetadata(X_AMZ_META + kv._1) = kv._2
  }

  def <<<(data: InputStream, length: Long) {
    create(data, length, (r: Request) => { r })
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
    val req = new Request(new URL("http://%s.s3.amazonaws.com/%s".format(key.bucket.name, key.name)))
    client.get(req, (r: Response) => {
      r.content match {
        case None =>
        case Some(is) => IOUtils.copy(is, out)
      }
    })
  }
  
  def delete(): Unit = {
    client.delete(new Request(new URL("http://%s.s3.amazonaws.com/%s".format(bucket.name, name))), (r: Response) => None)
  }

  override def toString() = key.toString()

  def withContentType(contentType: String): RichKey = {
    this.contentType = contentType
    this
  }

  def withExpires(expires: Long): RichKey = {
    this.expires = expires
    this
  }

  def withContentMD5(etag: String): RichKey = {
    this.contentMD5 = etag
    this
  }

  def withUserMetadata(kv: (String, String)): RichKey = {
    this.userMetadata = kv
    this
  }

  private def create(data: InputStream, length: Long, applyACL: (Request) => Request): RichKey = {
    val req = new Request(new URL("http://%s.s3.amazonaws.com/%s".format(key.bucket.name, key.name)))
    applyACL(req)
    client.put(req, (r: Response) => None)(data, length)
    this
  }

  private def head(): Map[String, String] = {
    client.head(new Request(new URL("http://%s.s3.amazonaws.com/%s".format(key.bucket.name, key.name))))._2
  }

}

class Key(val bucket: Bucket, val name: String,
  val contentType: Option[String] = None,
  val expires: Option[Long] = None,
  val size: Option[Long] = None,
  val contentMD5: Option[String] = None,
  val lastModified: Option[Date] = None,
  val userMetadata: Map[String, String] = Map()) {

  override def toString() = "Key[/%s/%s]".format(bucket.name, name)

}

object Key {
  implicit def key2RichKey(k: Key)(implicit client: HTTPClient): RichKey = {
    new RichKey(client, k)
  }

  implicit def richKey2Key(k: RichKey): Key = {
    new Key(k.bucket, k.name, k.contentType, k.expires, k.size, k.contentMD5, k.lastModified, k.userMetadata)
  }
}