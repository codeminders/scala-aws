package com.codeminders.scalaws.s3.api

import scala.xml._
import java.io.InputStream
import java.io.ByteArrayInputStream
import scala.io.Source
import com.codeminders.scalaws.http.HTTPClient
import com.codeminders.scalaws.http.Response
import java.net.URL
import java.util.Date
import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.s3.model.Key
import com.codeminders.scalaws.AmazonClientException
import com.codeminders.scalaws.utils.DateUtils
import com.codeminders.scalaws.s3.model.StorageClass.StorageClass
import com.codeminders.scalaws.s3.model.StorageClass
import com.codeminders.scalaws.s3.model.Owner
import com.codeminders.scalaws.s3.model.ObjectMetadata
import com.codeminders.scalaws.s3.Request
import com.codeminders.scalaws.s3.model.S3ObjectSummary
import com.codeminders.scalaws.utils.Utils

object Keys {
  
  private def list(client: HTTPClient, bucketName: String, delimiter: String = "", maxKeys: Int = 1000)(prefix: String = "", marker: String = ""): (Seq[S3ObjectSummary], Seq[String], Boolean) = {

    def extractKey(node: scala.xml.Node): S3ObjectSummary =
      node match {
        case <Contents><Key>{ name }</Key><LastModified>{ lastModifiedTS }</LastModified><ETag>{ etg }</ETag><Size>{ sz }</Size><Owner><ID>{ ownerId }</ID><DisplayName>{ ownerDisplayName }</DisplayName></Owner><StorageClass>{ sc }</StorageClass></Contents> =>
          new S3ObjectSummary(name.text){
              override val size: Long = sz.text.toLong
              override val etag: String = etg.text
              override val lastModified: Date = DateUtils.parseIso8601Date(lastModifiedTS.text)
              override val storageClass: StorageClass = StorageClass.withName(sc.text)
              override val owner: Owner = new Owner(ownerId.text, ownerDisplayName.text)
          }
      }

    val responseHandler = (r: Response) => {
      r.content match {
        case None => throw AmazonClientException("Could not parse an empty response from server")
        case Some(is) => {
          Utils.using(is){
            is => 
        	  XML.load(is)
          }
        }
      }
    }

    val xml = client.get(Request(bucketName, parameters=Array(("prefix", prefix), ("delimiter", delimiter), ("max-keys", maxKeys.toString) ,("marker", marker))), responseHandler)

    ((xml \ "Contents").foldLeft(Array[S3ObjectSummary]())((a, b) => a :+ extractKey(b)), (xml \ "CommonPrefixes" \ "Prefix").foldLeft(Array[String]())((a, b) => a :+ b.text), (xml \ "IsTruncated").text.toBoolean)
  }
  
  def apply(client: HTTPClient, bucket: Bucket, prefix: String = "", delimiter: String = "", maxKeys: Int = 1000, marker: String = ""): Keys = {
    val nextKeys = list(client, bucket, delimiter = delimiter, maxKeys = maxKeys)(_, _)
    val lbr = nextKeys(prefix, marker)
    new Keys(lbr._1, lbr._2, lbr._3, nextKeys, prefix)
  }
}

class Keys(keys: Seq[S3ObjectSummary], prefexes: Seq[String], hasNext: Boolean, nextKeys: (String, String) => (Seq[S3ObjectSummary], Seq[String], Boolean), val prefix: String) extends Stream[S3ObjectSummary] {

  lazy val commonPrefexes: Seq[Keys] = {
    if (!prefexes.isEmpty) {
      for (prefix <- prefexes) yield {
        val nk = nextKeys(prefix, "")
        new Keys(nk._1, nk._2, nk._3, nextKeys, prefix)
      }
    } else Seq.empty
  }

  override def tail: Keys = {
    if (keys.tail.isEmpty) {
        val nk = nextKeys(prefix, keys.last.key)
        new Keys(nk._1, nk._2, nk._3, nextKeys, prefix)
      } else {
      new Keys(keys.tail, prefexes, hasNext, nextKeys, prefix)
    }
  }

  override def isEmpty = keys.isEmpty && !hasNext

  override def head = {
    keys.head
  }

  override def tailDefined = !keys.tail.isEmpty && hasNext

}

