package com.codeminders.scalaws.s3.api

import com.codeminders.scalaws.http.HTTPClient
import com.codeminders.scalaws.http.Response
import com.codeminders.scalaws.utils.DateUtils
import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.s3.model.Key
import com.codeminders.scalaws.s3.model.ObjectMetadata
import com.codeminders.scalaws.s3.model.Expiration
import java.net.URL
import com.codeminders.scalaws.s3.Request

class RichKey(client: HTTPClient, val bucketName: String, val keyName: String) {
  
  def name = keyName

  def metadata: ObjectMetadata = {
     val req = Request(bucketName, keyName)
     extractObjectMetadata(client.head(req)._2)
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