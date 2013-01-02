package com.codeminders.scalaws.s3.api

import scala.xml._
import java.io.InputStream
import java.io.ByteArrayInputStream
import scala.io.Source
import com.codeminders.scalaws.http.HTTPClient
import com.codeminders.scalaws.http.Response
import java.net.URL
import java.util.Date
import com.codeminders.scalaws.http.Request
import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.AmazonClientException
import com.codeminders.scalaws.utils.DateUtils
import com.codeminders.scalaws.s3.model.StorageClass
import com.codeminders.scalaws.s3.model.Owner
import com.codeminders.scalaws.s3.model.ObjectMetadata

object KeysStream {
  
  private def list(client: HTTPClient, name: String, delimiter: String = "", maxKeys: Int = 1000)(prefix: String = "", marker: String = ""): (Seq[(String, ObjectMetadata)], Seq[String], Boolean) = {

    def extractKey(node: scala.xml.Node): (String, ObjectMetadata) =
      node match {
        case <Contents><Key>{ name }</Key><LastModified>{ lastModified }</LastModified><ETag>{ etag }</ETag><Size>{ size }</Size><Owner><ID>{ ownerId }</ID><DisplayName>{ ownerDisplayName }</DisplayName></Owner><StorageClass>{ storageClass }</StorageClass></Contents> =>
          (name.text, ObjectMetadata(size.text.toLong, etag.text, DateUtils.parseIso8601Date(lastModified.text), StorageClass.withName(storageClass.text), new Owner(ownerId.text, ownerDisplayName.text)))
      }

    val responseHandler = (r: Response) => {
      r.content match {
        case None => throw AmazonClientException("Could not parse an empty response from server")
        case Some(is) => XML.load(is)
      }
    }

    val xml = client.get(new Request(new URL("http://%s.s3.amazonaws.com/?prefix=%s&delimiter=%s&max-keys=%d&marker=%s".format(name, prefix, delimiter, maxKeys, marker))), responseHandler)

    ((xml \ "Contents").foldLeft(Array[(String, ObjectMetadata)]())((a, b) => a ++ Array(extractKey(b))), (xml \ "CommonPrefixes" \ "Prefix").foldLeft(Array[String]())((a, b) => a ++ Array(b.text)), (xml \ "IsTruncated").text.toBoolean)
  }
  
  def apply(client: HTTPClient, bucket: Bucket, prefix: String = "", delimiter: String = "", maxKeys: Int = 1000, marker: String = ""): KeysStream = {
    val nextKeys = list(client, bucket.name, delimiter = delimiter, maxKeys = maxKeys)(_, _)
    val lbr = nextKeys(prefix, marker)
    new KeysStream(lbr._1, lbr._2, lbr._3, nextKeys, prefix)
  }
}

class KeysStream(keys: Seq[(String, ObjectMetadata)], prefexes: Seq[String], hasNext: Boolean, nextKeys: (String, String) => (Seq[(String, ObjectMetadata)], Seq[String], Boolean), val prefix: String) extends Stream[(String, ObjectMetadata)] {

  lazy val commonPrefexes: Seq[KeysStream] = {
    if (!prefexes.isEmpty) {
      for (prefix <- prefexes) yield {
        val nk = nextKeys(prefix, keys.last._1)
        new KeysStream(nk._1, nk._2, nk._3, nextKeys, prefix)
      }
    } else Seq.empty
  }

  override def tail: KeysStream = {
    if (keys.tail.isEmpty) {
      val nk = nextKeys(prefix, keys.last._1)
      new KeysStream(nk._1, nk._2, nk._3, nextKeys, prefix)
    } else {
      new KeysStream(keys.tail, prefexes, hasNext, nextKeys, prefix)
    }
  }

  override def isEmpty = keys.isEmpty && !hasNext

  override def head = {
    keys.head
  }

  override def tailDefined = !keys.tail.isEmpty && hasNext

}

