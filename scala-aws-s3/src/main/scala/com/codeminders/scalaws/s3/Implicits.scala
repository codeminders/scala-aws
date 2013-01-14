package com.codeminders.scalaws.s3

import com.codeminders.scalaws.s3.model.S3ObjectBuilder
import org.apache.commons.io.IOUtils
import com.codeminders.scalaws.s3.model.Key
import java.io.File
import java.io.FileInputStream
import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.s3.api.RichBucket
import com.codeminders.scalaws.s3.api.RichS3Object
import com.codeminders.scalaws.s3.model.S3Object
import scala.io.Source
import java.io.InputStream

object Implicits {
  
  implicit def string2Key(keyName: String): Key = {
    new Key(keyName)
  }
  
  implicit def string2Bucket(bucketName: String): Bucket = {
    new Bucket(bucketName)
  }
  
  implicit def string2S3ObjectBuilder(data: String): S3ObjectBuilder = {
    S3ObjectBuilder(data)
  }
  
  implicit def file2S3ObjectBuilder(file: File): S3ObjectBuilder = {
    S3ObjectBuilder(file)
  }
  
  implicit def s3Object2S3ObjectBuilder(obj: S3Object): S3ObjectBuilder = {
    S3ObjectBuilder(obj)
  }
  
  implicit def richS3Object2S3ObjectBuilder(obj: RichS3Object): S3ObjectBuilder = {
    S3ObjectBuilder(richS3Object2S3Object(obj))
  }
  
  implicit def sourceAndLong2S3ObjectBuilder(data: (Source, Long)): S3ObjectBuilder = {
    S3ObjectBuilder(data._1, data._2)
  }
  
  implicit def inputStreamAndLong2S3ObjectBuilder(data: (InputStream, Long)): S3ObjectBuilder = {
    S3ObjectBuilder(data._1, data._2)
  }
  
  implicit def richBucket2Bucket(b : RichBucket): Bucket = {
	  b.bucket
  }
  
  implicit def richS3Object2S3Object(obj : RichS3Object): S3Object = {
	  new S3Object(obj.bucket, obj.key, obj.content, obj.contentLength)
  }
  
}