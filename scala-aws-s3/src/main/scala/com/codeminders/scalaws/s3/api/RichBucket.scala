package com.codeminders.scalaws.s3.api

import scala.xml._
import java.io.InputStream
import java.io.ByteArrayInputStream
import scala.io.Source
import com.codeminders.scalaws.http.HTTPClient
import com.codeminders.scalaws.http.Response
import java.net.URL
import com.codeminders.scalaws.AmazonClientException
import com.codeminders.scalaws.s3.model.CannedACL._
import com.codeminders.scalaws.s3.model.Permission._
import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.s3.model.ObjectMetadata
import com.codeminders.scalaws.s3.model.ACL
import com.codeminders.scalaws.s3.model.Key
import com.codeminders.scalaws.s3.model.Region._
import sun.security.ssl.ByteBufferInputStream
import org.apache.commons.io.IOUtils
import java.util.Date
import com.codeminders.scalaws.utils.DateUtils
import com.codeminders.scalaws.s3.model.S3Object
import com.codeminders.scalaws.s3.model.S3ObjectBuilder
import com.codeminders.scalaws.s3.model.Expiration
import com.codeminders.scalaws.AmazonServiceException
import java.io.File
import java.io.FileInputStream
import com.codeminders.scalaws.s3.Request
import com.codeminders.scalaws.helpers.io.EmptyInputStream
import com.codeminders.scalaws.s3.model.MultipartUploadBuilder
import com.codeminders.scalaws.utils.Utils
import com.codeminders.scalaws.s3.model.MultipartUploadSummary
import com.codeminders.scalaws.s3.model.MultipartUpload
import scala.collection.mutable.ArrayBuffer
import com.codeminders.scalaws.s3.model.Owner
import com.codeminders.scalaws.s3.model.StorageClass

class RichBucket(client: HTTPClient, val bucket: Bucket) {
  
  private val FIVE_GB = 1024 * 1024 * 1024 * 5

  def name = bucket.name

  def region = bucket.region

  def delete(key: Key): Unit = {
    client.delete(Request(bucket.name, key.name), (r: Response) => None)
  }

  def update(key: Key, objectBuilder: S3ObjectBuilder): RichS3Object = {
	 val req = Request(name, key.name, headers = objectBuilder.metadata)
	 client.put(req, (r: Response) => None)(objectBuilder.content, objectBuilder.contentLength)
	 new RichS3Object(this.client, this.bucket, key)
  }
  
  def update(key: Key, data: InputStream): RichS3Object = {
    Utils.using(new MultipartUploadOutputStream(initiateUpload(key))){
        outputStream =>
          IOUtils.copy(data, outputStream)
      }
      new RichS3Object(this.client, this.bucket, key)
  }
  
  def apply(key: Key): RichS3Object = {
    new RichS3Object(this.client, this.bucket, key)
  }
  
  def initiateUpload(key: Key, builder: MultipartUploadBuilder = new MultipartUploadBuilder): RichMultipartUpload = {
    new RichMultipartUpload(client, this.bucket, key)
  }
  
  def listUploads(prefix: String = "", delimiter: String = "", keyMarker: String = "", uploadIdMarker: String = "", maxUploads: Int = 1000): MultipartUploads = {
    MultipartUploads(listMultipartUploads(delimiter, maxUploads)(_, _, _), prefix, keyMarker, uploadIdMarker)
  }

  def list(prefix: String = "", delimiter: String = "", maxKeys: Int = 1000, marker: String = ""): Keys = {
    Keys(client, bucket, prefix, delimiter, maxKeys, marker)
  }

  def exist(key: Key): Boolean = {
    try {
      client.head(Request(bucket.name, key.name))
      true
    } catch {
      case e: AmazonServiceException => if (e.statusCode == 404) false else throw e
      case e => throw e
    }
  }

  def acl: ACL = {

    val req = Request(bucket.name, parameters = Array(("acl", "")))
    ACL(client.get(req, (r: Response) => {
      r.content match {
        case None => throw AmazonClientException("Could not parse an empty response from server")
        case Some(is) => XML.load(is)
      }
    }))
  }

  def acl_=(newACL: ACL) = {
    val r = Request(bucket.name, parameters = Array(("acl", "")))
    val data = newACL.toXML.buildString(true)
    client.put(r, (r: Response) => None)(IOUtils.toInputStream(data), data.length())
  }

  def acl_=(newACL: CannedACL) = {
    val r = Request(bucket.name, parameters = Array(("acl", "")), headers = Array(("x-amz-acl", newACL.toString())))
    client.put(r, (r: Response) => None)(EmptyInputStream(), 0)
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
    val r = Request(bucket.name, parameters = Array(("acl", "")), headers = aclHeaders)
    client.put(r, (r: Response) => None)(IOUtils.toInputStream(""), 0)
  }
  
  def owner = acl.owner
  
  override def toString() = bucket.toString()
  
  private def listMultipartUploads(delimiter: String = "", maxUploads: Int = 1000)(prefix: String = "", keyMarker: String = "", uploadIdMarker: String = ""): (Seq[MultipartUpload with MultipartUploadSummary], Seq[String], Boolean) = {
    def extractPart(node: scala.xml.Node): MultipartUpload with MultipartUploadSummary = {
      node match {
        case <Upload><Key>{ keyName }</Key><UploadId>{ uploadId }</UploadId><Initiator><ID>{ initiatorId }</ID><DisplayName>{ initiatorDisplayName }</DisplayName></Initiator><Owner><ID>{ ownerId }</ID><DisplayName>{ ownerDisplayName }</DisplayName></Owner><StorageClass>{ storageClassName }</StorageClass><Initiated>{ initiatedDate }</Initiated></Upload> =>
          new MultipartUpload(this.bucket, new Key(keyName.text), uploadId.text) with MultipartUploadSummary {
            override val initiator: Owner = new Owner(initiatorId.text, initiatorDisplayName.text)
			  override val owner: Owner = new Owner(ownerId.text, ownerDisplayName.text)
			  override val storageClass: StorageClass.StorageClass = StorageClass.withName(storageClassName.text)
			  override val initiated: Date = DateUtils.parseIso8601Date(initiatedDate.text)
          }
    	}      
    }
    
    val req = Request(bucket.name, parameters=Array(
        ("uploads", ""),
        ("delimiter", delimiter),
        ("max-uploads", maxUploads.toString),
        ("key-marker", keyMarker.toString()),
        ("prefix", prefix.toString()),
        ("upload-id-marker", uploadIdMarker.toString()))
    )
    val xml = client.get(req, (r: Response) => {
      r.content match {
        case None => throw AmazonClientException("Could not parse an empty response from server")
        case Some(is) => XML.load(is)
      }
    })
    
    ((xml \ "Upload").foldLeft(ArrayBuffer[MultipartUpload with MultipartUploadSummary]())((a, b) => a += extractPart(b)).toSeq,
        (xml \ "CommonPrefixes" \ "Prefix").foldLeft(ArrayBuffer[String]())((a, b) => a += b.text).toSeq, 
        (xml \ "IsTruncated").text.toBoolean)
  }

}