package com.codeminders.scalaws.s3

import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.s3.model.S3Object

package object api {
  
  implicit def richBucket2Bucket(b : RichBucket): Bucket = {
	  b.bucket
  }
  
  implicit def richS3Object2S3Object(obj : RichS3Object): S3Object = {
	  new S3Object(obj.bucket, obj.key, obj.content, obj.contentLength)
  }
  
}