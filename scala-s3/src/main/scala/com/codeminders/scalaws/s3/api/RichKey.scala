package com.codeminders.scalaws.s3.api

import java.io.InputStream
import java.io.OutputStream
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import scala.io.Source
import java.net.URL
import java.io.BufferedInputStream
import com.codeminders.scalaws.s3.http.Response
import scala.collection._
import java.util.Date
import scala.collection.immutable.Map
import com.codeminders.scalaws.s3.http.HTTPClient
import com.codeminders.scalaws.s3.http.Request
import com.codeminders.scalaws.s3.AmazonClientException
import com.codeminders.scalaws.s3.DateUtils
import com.codeminders.scalaws.s3.model.Key
import com.codeminders.scalaws.s3.model.CannedACL
import com.codeminders.scalaws.s3.model.ExplicitACL
import com.codeminders.scalaws.s3.model.ObjectMetadata
import sun.security.ssl.ByteBufferInputStream

class RichKey(client: HTTPClient, key: Key) {

  private val CONTENT_TYPE = "Content-Type"
  private val EXPIRATION = "x-amz-expiration"
  private val OBJECT_SIZE = "Content-Length"
  private val ETAG = "ETag"
  private val LAST_MODIFIED = "Last-Modified"
  private val CONTENT_MD5 = "Content-MD5"
  private val DATE = "Date"
  private val X_AMZ_META = "x-amz-meta-"

  def metadata = {
    val m = head
    new ObjectMetadata(
    		m.get(CONTENT_TYPE),
    		m.get(EXPIRATION) match {
    		  case None => None
    		  case Some(s) => Option(s.toLong)
    		},
    		m.get(OBJECT_SIZE) match {
    		  case None => None
    		  case Some(s) => Option(s.toLong)
    		},
    		m.get(ETAG) match {
    		  case None => None
    		  case Some(s) => Option(s.replaceAll("\"", ""))
    		},
    		m.get(LAST_MODIFIED) match {
    		  case None => None
    		  case Some(s) => Option(DateUtils.parseRfc822Date(s))
    		},
    		m.foldLeft(Map[String, String]()){
    		  (m, kv) => if(kv._1.startsWith(X_AMZ_META)) m + (kv._1.substring(X_AMZ_META.length()) -> kv._2) else m
    		}
        )
  }

  lazy val bucket = key.bucket

  lazy val name = key.name
  
  def create(): RichKey = {
    create(new ByteArrayInputStream(Array[Byte]()), 0, (r: Request) => { r })
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