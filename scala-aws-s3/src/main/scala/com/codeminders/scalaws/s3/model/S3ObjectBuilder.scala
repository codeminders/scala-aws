package com.codeminders.scalaws.s3.model

import java.io.InputStream
import scala.collection.immutable.Map
import scala.collection._
import StorageClass._
import Permission._
import CannedACL._
import ServerSideEncryption._
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import scala.io.Source
import com.codeminders.scalaws.helpers.io.SourceInputStream
import com.codeminders.scalaws.helpers.io.EmptyInputStream

class S3ObjectBuilder private {
  
  var content: InputStream = EmptyInputStream()
  
  var copySource: Option[String] = None
  
  var contentLength: Long = 0
  
  private def this(content: InputStream, contentLength: Long) = {
    this()
    this.content = content
    this.contentLength = contentLength
  }
  
  private def this(src: String) = {
    this()
    this.copySource = Option(src)
  }
  
  private var cannedACL: Option[CannedACL] = None
  
  private var explicitACL: mutable.Map[Permission, List[String]] = mutable.Map.empty
  
  private var userMetadata: Set[(String, String)] = Set.empty
  
  private var expiration: Option[Long] = None
  
  private var serverSideEncryption: Option[ServerSideEncryption] = None
  
  private var storageClass: Option[StorageClass] = None
  
  private var cacheControl: Option[String] = None
  
  private var contentDisposition: Option[String] = None
  
  private var contentEncoding: Option[String] = None
  
  private var contentMD5: Option[String] = None
  
  private var contentType: Option[String] = None
  
  def withCannedACL(cannedACL: CannedACL): S3ObjectBuilder = {
    explicitACL = mutable.Map.empty
    this.cannedACL = Option(cannedACL)
    this
  }
  
  def withExplicitACL(p: Permission, uid: String): S3ObjectBuilder = {
    cannedACL = None
    explicitACL.get(p) match {
      case None => explicitACL(p) = List(uid)
      case Some(l) => explicitACL(p) = uid :: l
    }
    this
  }
  
  def withContentType(contentType: String): S3ObjectBuilder = {
    this.contentType = Option(contentType)
    this
  }
  
  def withMetadata(key: String, value: String): S3ObjectBuilder = {
    this.userMetadata += Tuple2(key, value)
    this
  }
  
  def metadata: Seq[(String, String)] = {
    var r = mutable.Set.empty[(String, String)]
    if(cannedACL != None){
      r += Tuple2("x-amz-acl", cannedACL.get.toString)
    } else if(!explicitACL.isEmpty){
      for((p, uids) <- explicitACL){
        p match {
          case READ => uids.foreach(u => r += Tuple2("x-amz-grant-read", u))
          case WRITE => uids.foreach(u => r += Tuple2("x-amz-grant-write", u))
          case READ_ACP => uids.foreach(u => r += Tuple2("x-amz-grant-read-acp", u))
          case WRITE_ACP => uids.foreach(u => r += Tuple2("x-amz-grant-write-acp", u))
          case FULL_CONTROL => uids.foreach(u => r += Tuple2("x-amz-grant-full-control", u))
        }
      }
    }
    
    if(copySource != None) {
      r += Tuple2("x-amz-copy-source", copySource.get)
    }
    
    if(!userMetadata.isEmpty){
      for(meta <- userMetadata){
    	  r += Tuple2("x-amz-meta-" + meta._1, meta._2)
      }
    }
    if(contentType != None) {
      r += Tuple2("Content-Type", contentType.get)
    }
    if(expiration != None){
      r += Tuple2("Expires", expiration.get.toString)
    }
    if(serverSideEncryption != None){
      r += Tuple2("x-amz-server-side​-encryption", serverSideEncryption.get.toString)
    }
    if(serverSideEncryption != None){
      r += Tuple2("x-amz-storage-class", storageClass.get.toString)
    }
    if(cacheControl != None){
      r += Tuple2("Cache-Control", cacheControl.get)
    }
    if(contentDisposition != None){
      r += Tuple2("Content-Disposition", contentDisposition.get)
    }
    if(contentEncoding != None){
      r += Tuple2("Content-Encoding", contentEncoding.get)
    }
    if(contentMD5 != None){
      r += Tuple2("Content-MD5", contentMD5.get)
    }
    r.toList
  }
  
}

object S3ObjectBuilder{
  def apply(data: String): S3ObjectBuilder = {
    new S3ObjectBuilder(IOUtils.toInputStream(data), data.length())
  }
  
  def apply(data: File): S3ObjectBuilder = {
    new S3ObjectBuilder(new FileInputStream(data), data.length())
  }
  
  def apply(data: Source, length: Long): S3ObjectBuilder = {
    new S3ObjectBuilder(new SourceInputStream(data), length)
  }
  
  def apply(data: InputStream, length: Long): S3ObjectBuilder = {
    new S3ObjectBuilder(data, length)
  }
  
  def apply(obj: S3Object): S3ObjectBuilder = {
    new S3ObjectBuilder("%s/%s".format(obj.bucket, obj.key))
  }
  
}
    