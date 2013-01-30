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

    def extractKey(node: scala.xml.Node): S3ObjectSummary = {
      val name = node \ "Key"
      val lastModifiedTS = node \ "LastModified"
      val etg = node \ "ETag"
      val sz = node \ "Size"
      val sc = node \ "StorageClass"
      val ownerDisplayName = node \ "Owner" \ "ID"
      val ownerId = node \ "Owner" \ "DisplayName"

      new S3ObjectSummary(name.text) {
        override val size: Long = sz.text.toLong
        override val etag: String = etg.text
        override val lastModified: Date = DateUtils.parseIso8601Date(lastModifiedTS.text)
        override val storageClass: StorageClass = StorageClass.withName(sc.text)
        override val owner: Owner = new Owner("", "")
      }
    }

    val responseHandler = (r: Response) => {
      r.content match {
        case None => throw AmazonClientException("Could not parse an empty response from server")
        case Some(is) => {
          Utils.using(is) {
            is =>
              XML.load(is)
          }
        }
      }
    }

    val xml = client.get(Request(bucketName, parameters = Array(("prefix", prefix), ("delimiter", delimiter), ("max-keys", maxKeys.toString), ("marker", marker))), responseHandler)

    ((xml \ "Contents").foldLeft(Array[S3ObjectSummary]())((a, b) => a :+ extractKey(b)), (xml \ "CommonPrefixes" \ "Prefix").foldLeft(Array[String]())((a, b) => a :+ b.text), (xml \ "IsTruncated").text.toBoolean)
  }

  def apply(client: HTTPClient, bucket: Bucket, prefix: String = "", delimiter: String = "", maxKeys: Int = Int.MaxValue, marker: String = ""): Keys = {
    require(maxKeys > 0, "maxKeys should be greater than 0")
    val nextFunc = list(client, bucket, delimiter = delimiter, maxKeys=math.min(maxKeys, 1000))(_, _)
    new Keys(prefix, nextFunc) {
      override protected def next = nextFunc(prefix, marker)
    }
  }
}

abstract class Keys(val prefix: String, nextKeys: (String, String) => (Seq[S3ObjectSummary], Seq[String], Boolean)) extends Stream[Either[S3ObjectSummary, Keys]] {
  
  protected def next: (Seq[S3ObjectSummary], Seq[String], Boolean)
  
  lazy val keys = this.filter(_.isLeft).map(_.left.get)
  
  lazy val commonPrefexes = this.filter(_.isRight).map(_.right.get)
  
  private lazy val _next = next

  private lazy val _keys = _next._1

  private lazy val prefexes = _next._2

  private lazy val hasNext = _next._3
  
  override def tail: Keys = {
    if(_keys.length + prefexes.length > 1){
      if(!_keys.isEmpty && !prefexes.isEmpty){
        if(_keys.head.key < prefexes.head){
          newKeys(prefix, (_keys.tail, prefexes, hasNext))
        }else {
          newKeys(prefix, (_keys, prefexes.tail, hasNext))
        }
      } else if(!_keys.isEmpty){
        newKeys(prefix, (_keys.tail, prefexes, hasNext))
      } else {
        newKeys(prefix, (_keys, prefexes.tail, hasNext))
      }
    } else if(hasNext) {
      if(!_keys.isEmpty){
        newKeys(prefix, nextKeys(prefix, _keys.head.key))
      } else {
    	newKeys(prefix, nextKeys(prefix, prefexes.head))
      }
    } else {
      newKeys(prefix, (Seq.empty[S3ObjectSummary], Seq.empty[String], false))
    }
  }

  override def isEmpty = _keys.isEmpty && prefexes.isEmpty

  override def head = {
    if(_keys.isEmpty && prefexes.isEmpty) throw AmazonClientException("Can not get head of an empty stream")
    else if(!_keys.isEmpty && !prefexes.isEmpty){
      if (_keys.head.key < prefexes.head) Left(_keys.head)
      else Right(newKeys(prefexes.head, nextKeys(prefexes.head, "")))
    }
    else if (!_keys.isEmpty) Left(_keys.head)
    else Right(newKeys(prefexes.head, nextKeys(prefexes.head, "")))
  }

  override def tailDefined = {
    (keysTailDefined || prefexesTailDefined || hasNext)
  }

  private def keysTailDefined = {
    !_keys.isEmpty && !_keys.tail.isEmpty
  }
  
  private def prefexesTailDefined = !prefexes.isEmpty && !prefexes.tail.isEmpty
  
  private def newKeys(p: String, nxt: => (Seq[S3ObjectSummary], Seq[String], Boolean)) = {
    new Keys(p, nextKeys) {
      override protected def next ={
        nxt
      } 
    }
  }
  
  private def isHeadComesFirst: Boolean = {
    if(_keys.isEmpty) false
    else if(prefexes.isEmpty) true
    else if(_keys.head.key < prefexes.head) true
    else false
  }

}