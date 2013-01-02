package com.codeminders.scalaws.s3.api

import com.codeminders.scalaws.http.HTTPClient
import com.codeminders.scalaws.http.Request
import com.codeminders.scalaws.http.Response
import com.codeminders.scalaws.s3.model.S3Object
import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.s3.model.Key
import com.codeminders.scalaws.s3.model.ObjectMetadata
import com.codeminders.scalaws.s3.model.Expiration
import com.codeminders.scalaws.utils.DateUtils
import java.io.InputStream
import java.net.URL

class RichS3Object(client: HTTPClient, val bucket: Bucket, val key: Key) {

  private var headers: Map[String, String] = Map.empty

  def metadata(key: Key): ObjectMetadata = {

    val req = new Request(new URL("http://%s.s3.amazonaws.com/%s".format(bucket.name, key.name)))
    extractObjectMetadata(client.head(req)._2)
  }

  lazy val (content, contentLength) = {
    val req = new Request(new URL("http://%s.s3.amazonaws.com/%s".format(bucket.name, key.name)))
    client.get(req, (r: Response) => {
      headers = r.headers
      (r.content.get, r("Content-Length").toLong)
    })
  }

  lazy val metadata: ObjectMetadata = {
    if (headers.isEmpty) {
      val req = new Request(new URL("http://%s.s3.amazonaws.com/%s".format(bucket.name, key.name)))
      extractObjectMetadata(client.head(req)._2)
    } else {
      extractObjectMetadata(headers)
    }
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
      None,
      None,
      headers.get("x-amz-expiration") match {
        case None => None
        case Some(exp) => Expiration(exp)
      },
      Option(headers("Content-Type")),
      headers.get("x-amz-version-id"))
  }

}