package com.codeminders.scalaws.s3.model

import java.io.InputStream

class S3ObjectBuilder(var content: InputStream, var contentLength: Long) {
  
  def s3Object(bucket: Bucket, key: Key): S3Object = {
    new S3Object(bucket, key, content, contentLength)
  }
}