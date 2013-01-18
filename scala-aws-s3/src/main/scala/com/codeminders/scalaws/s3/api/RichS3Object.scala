package com.codeminders.scalaws.s3.api

import com.codeminders.scalaws.http.HTTPClient
import com.codeminders.scalaws.http.Response
import com.codeminders.scalaws.s3.model.S3Object
import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.s3.model.Key
import com.codeminders.scalaws.s3.model.ObjectMetadata
import com.codeminders.scalaws.s3.model.Expiration
import com.codeminders.scalaws.utils.DateUtils
import java.io.InputStream
import java.net.URL
import com.codeminders.scalaws.s3.Request
import com.codeminders.scalaws.s3.model.ACL
import com.codeminders.scalaws.s3.model.CannedACL._
import com.codeminders.scalaws.s3.model.Permission._
import com.codeminders.scalaws.AmazonClientException
import scala.xml.XML
import com.codeminders.scalaws.s3.model.Owner
import com.codeminders.scalaws.s3.model.Grant
import com.codeminders.scalaws.s3.model.CanonicalGrantee
import com.codeminders.scalaws.s3.model.Permission
import com.codeminders.scalaws.s3.model.EmailAddressGrantee
import com.codeminders.scalaws.s3.model.GroupGrantee
import scala.collection.immutable.Set
import org.apache.commons.io.IOUtils
import com.codeminders.scalaws.s3.model.MultipartUploadBuilder
import com.codeminders.scalaws.s3.model.MultipartUploadBuilder

class RichS3Object(client: HTTPClient, val bucket: Bucket, val key: Key) {

  def content(off: Int = 0, len: Long = -1): (InputStream, Long, Long) = {
    require(off >= 0, "Offset could not be a negative value")
    val req = if (len < 0 && off == 0) Request(bucket.name, key.name)
    else Request(bucket.name, key.name, headers = Array(("Range", "bytes=%d-%s".format(off, if (len <= 0) "" else len + off - 1))))
    client.get(req, (r: Response) => {
      val bytesRead = r("Content-Length") match {
          case None => throw AmazonClientException("Got Get object response misses Content-Length header")
          case Some(l) => l.toLong
        }
      r("Content-Range") match {
        case None => (r.content.get, bytesRead, 0)
        case Some(v) => (r.content.get, bytesRead, v.split("/", 2)(1).toLong - (off + bytesRead))
      }
    })
  }

  def inputStream(off: Int = 0, len: Long = -1) = content(off, len)._1

  def inputStream = content()._1

  def metadata: ObjectMetadata = {
    val req = Request(bucket.name, key.name)
    extractObjectMetadata(client.head(req)._2)
  }

  def acl: ACL = {

    val req = Request(bucket.name, key.name, Array(("acl", "")))
    ACL(client.get(req, (r: Response) => {
      r.content match {
        case None => throw AmazonClientException("Could not parse an empty response from server")
        case Some(is) => XML.load(is)
      }
    }))
  }

  def acl_=(newACL: ACL) = {
    val r = Request(bucket.name, key.name, Array(("acl", "")))
    val data = newACL.toXML.buildString(true)
    client.put(r, (r: Response) => None)(IOUtils.toInputStream(data), data.length())
  }

  def acl_=(newACL: CannedACL) = {
    val r = Request(bucket.name, key.name, Array(("acl", "")), Array(("x-amz-acl", newACL.toString())))
    client.put(r, (r: Response) => None)(IOUtils.toInputStream(""), 0)
  }

  def acl_=(newACL: Map[Permission, Seq[String]]) = {
    require(!newACL.isEmpty, "Could not set ACL from empty value")
    val aclHeaders = newACL.foldLeft(Array.empty[(String, String)]) {
      (a, e) =>
        e._1 match {
          case READ => a ++ e._2.foldLeft(Array.empty[(String, String)])((arr, el) => arr :+ Tuple2("x-amz-grant-read", el))
          case WRITE => a ++ e._2.foldLeft(Array.empty[(String, String)])((arr, el) => arr :+ Tuple2("x-amz-grant-write", el))
          case READ_ACP => a ++ e._2.foldLeft(Array.empty[(String, String)])((arr, el) => arr :+ Tuple2("x-amz-grant-read-acp", el))
          case WRITE_ACP => a ++ e._2.foldLeft(Array.empty[(String, String)])((arr, el) => arr :+ Tuple2("x-amz-grant-write-acp", el))
          case FULL_CONTROL => a ++ e._2.foldLeft(Array.empty[(String, String)])((arr, el) => arr :+ Tuple2("x-amz-grant-full-control", el))
        }
    }
    val r = Request(bucket.name, key.name, Array(("acl", "")), aclHeaders)
    client.put(r, (r: Response) => None)(IOUtils.toInputStream(""), 0)
  }

  private def extractObjectMetadata(headers: Map[String, String]): ObjectMetadata = {
    def Expiration(str: String): Option[Expiration] = {
      val expirationRE = """expiry-date[=]["]([^"]+)["]\s*[,]\s*rule-id[=]["]([^"]+)["]""".r
      str match {
        case expirationRE(date, rule) => Option(new Expiration(DateUtils.parseRfc822Date(date), rule))
        case _ => None
      }
    }

    def ContentMD5(str: String): Option[String] = {
      val contentTypeRE = """["]?([a-f0-9]+)["]?""".r
      str match {
        case contentTypeRE(contentType) => Option(contentType)
        case _ => None
      }
    }

    new ObjectMetadata(
      Option(headers("Content-Length").toLong),
      ContentMD5(headers("ETag")),
      Option(DateUtils.parseRfc822Date(headers("Last-Modified"))),
      headers.get("x-amz-expiration") match {
        case None => None
        case Some(exp) => Expiration(exp)
      },
      Option(headers("Content-Type")),
      headers.get("x-amz-version-id"),
      headers.filter(kv => kv._1.startsWith("x-amz-meta-")).foldLeft(Array.empty[(String, String)])((s, kv) => s :+ (kv._1.substring(11), kv._2)))
  }

}