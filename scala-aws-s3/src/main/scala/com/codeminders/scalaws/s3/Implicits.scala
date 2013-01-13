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

object Implicits {
  
  implicit def string2Key(keyName: String): Key = {
    new Key(keyName)
  }
  
  implicit def string2Bucket(bucketName: String): Bucket = {
    new Bucket(bucketName)
  }
  
  implicit def string2S3ObjectBuilder(data: String): S3ObjectBuilder = {
    new S3ObjectBuilder(IOUtils.toInputStream(data), data.length())
  }
  
  implicit def file2S3ObjectBuilder(file: File): S3ObjectBuilder = {
    new S3ObjectBuilder(new FileInputStream(file), file.length())
  }
  
  implicit def key2S3ObjectBuilder(data: Key): S3ObjectBuilder = {
    new S3ObjectBuilder(IOUtils.toInputStream(data.name), data.name.length())
  }
  
  implicit def richBucket2Bucket(b : RichBucket): Bucket = {
	  b.bucket
  }
  
  implicit def richS3Object2S3Object(obj : RichS3Object): S3Object = {
	  new S3Object(obj.bucket, obj.key, obj.content, obj.contentLength)
  }
  
}