package com.codeminders.scalaws.s3.api

import com.codeminders.scalaws.s3.Request
import com.codeminders.scalaws.http.HTTPClient
import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.s3.model.Key
import com.codeminders.scalaws.http.Response
import com.codeminders.scalaws.AmazonClientException
import com.codeminders.scalaws.helpers.io.EmptyInputStream
import scala.xml.XML
import scala.collection.mutable.SetLike
import java.io.InputStream
import org.apache.commons.io.IOUtils
import scala.collection.mutable.ListBuffer
import scala.collection.immutable.LinearSeq
import scala.collection.immutable.Traversable
import scala.collection.mutable.ArrayBuffer
import com.codeminders.scalaws.helpers.collection.IndexedBuffer
import com.codeminders.scalaws.s3.model.MultipartUpload
import java.io.Closeable
import com.codeminders.scalaws.s3.model.MultipartUploadPart
import com.codeminders.scalaws.utils.DateUtils

class RichMultipartUpload(client: HTTPClient, bucket: Bucket, key: Key) extends RichS3Object(client, bucket, key) with Closeable {

  private val MAX_PART_NUMBER = 1048576 //Given that max supported file size is 5Tb and min supported part size is 5Mb

  private val MIN_PART_SIZE = 5 * 1024 * 1024;

  private val etags: IndexedBuffer[String] = new IndexedBuffer()

  val uploadID = initiateMultipartUpload

  private var _etag: Option[String] = None

  private var completed = false

  def update(idx: Int, data: (InputStream, Int)): RichMultipartUpload = {
    if (!completed) {
      requireIdx(idx)
      etags(idx) = uploadPart(idx, data._1, data._2)
    }
    this
  }

  def update(idx: Int, obj: RichS3Object, off: Long = 0, len: Long = -1): RichMultipartUpload = {
    if (!completed) {
      requireIdx(idx)
      etags(idx) = copyPart(idx, obj, off, len)
    }
    this
  }

  def isCompleted = completed
  
  def list(maxParts: Int = 1000, partNumberMarker: Int = -1): MultipartUploadParts = {
    MultipartUploadParts(listMultipartUploadParts(maxParts)(_))
  }

  def complete: RichS3Object = {
    if (!completed) {
      _etag = Option(completeMultipartUpload)
      completed = true
    }
    new RichS3Object(client, bucket, key)
  }

  def abort {
    if (!completed) {
      val req = Request(bucket.name, key.name, Array(("uploadId", uploadID)))
      client.delete(req, (r: Response) => None)
      completed = true
    }
  }

  def etag = _etag

  def close = {
    try {
      complete
    } catch {
      case e => {
        abort
        throw e
      }
    }
  }

  def toMultipartUpload: MultipartUpload = {
    new MultipartUpload(bucket, key, uploadID)
  }

  private def requireIdx(idx: Int) = require(idx >= 1 && idx <= MAX_PART_NUMBER, "Parameter idx of the method %s.upload should not be less than 1 and greater than %d".format(getClass.getName(), MAX_PART_NUMBER))

  private def copyPart(partNumber: Int, obj: RichS3Object, off: Long = 0, len: Long = -1): String = {
    require(off >= 0, "Offset could not be a negative value")
    val req = if (off == 0 && len <= 1) Request(this.bucket.name, this.key.name, Array(("partNumber", partNumber.toString), ("uploadId", uploadID)),
      headers = Array(("x-amz-copy-source", "/%s/%s".format(obj.bucket.name, obj.key.name))))
    else {
      val actualLength = if (len <= 1) obj.metadata.size.get else len
      Request(this.bucket.name, this.key.name, Array(("partNumber", partNumber.toString), ("uploadId", uploadID)),
        headers = Array(("x-amz-copy-source", "/%s/%s".format(obj.bucket.name, obj.key.name)), ("x-amz-copy-source-range", "bytes=%d-%d".format(off, off + actualLength - 1))))
    }
    client.put(req, (r: Response) => XML.load(r.content.get) \\ "ETag" text)(EmptyInputStream(), 0)
  }

  private def initiateMultipartUpload: String = {
    val req = Request(bucket.name, key.name, Array(("uploads", "")))
    client.post(req, (r: Response) => XML.load(r.content.get) \\ "UploadId" text)(EmptyInputStream(), 0)
  }

  private def uploadPart(partNumber: Int, data: InputStream, size: Long): String = {
    val req = Request(bucket.name, key.name, Array(("partNumber", partNumber.toString), ("uploadId", uploadID)))
    client.put(req, (r: Response) => r("ETag") match {
      case None => throw AmazonClientException("Put Upload Part response doesn't contain Etag header")
      case Some(v) => v
    })(data, size)
  }

  private def completeMultipartUpload: String = {
    val req = Request(bucket.name, key.name, Array(("uploadId", uploadID)))
    val xml = scala.xml.Utility.trim(
      <CompleteMultipartUpload>
        {
          for (etag <- etags) yield {
            <Part>
              <PartNumber>{ etag._1 }</PartNumber>
              <ETag>{ etag._2 }</ETag>
            </Part>
          }
        }
      </CompleteMultipartUpload>).buildString(true)
    client.post(req, (r: Response) => XML.load(r.content.get) \\ "ETag" text)(IOUtils.toInputStream(xml), xml.length)
  }
  
  private def listMultipartUploadParts(maxParts: Int = 1000)(partNumberMarker: Int = -1): (Seq[MultipartUploadPart], Boolean) = {
    
    def extractPart(node: scala.xml.Node): MultipartUploadPart =
      node match {
        case <Part><PartNumber>{ number }</PartNumber><LastModified>{ lastModified }</LastModified><ETag>{ etag }</ETag><Size>{ size }</Size></Part> =>
          new MultipartUploadPart(number.text.toInt, DateUtils.parseIso8601Date(lastModified.text), etag.text.replaceAll("[\"]", ""), size.text.toInt)
      }
    
    val req = if(partNumberMarker > 0)Request(bucket.name, key.name, Array(("uploadId", uploadID), ("max-parts", maxParts.toString), ("part-number-marker", partNumberMarker.toString())))
    else Request(bucket.name, key.name, Array(("uploadId", uploadID), ("max-parts", maxParts.toString)))
    val xml = client.get(req, (r: Response) => {
      r.content match {
        case None => throw AmazonClientException("Could not parse an empty response from server")
        case Some(is) => XML.load(is)
      }
    })
    
    ((xml \ "Part").foldLeft(ArrayBuffer[MultipartUploadPart]())((a, b) => a += extractPart(b)).toSeq, (xml \ "IsTruncated").text.toBoolean)
  }

}