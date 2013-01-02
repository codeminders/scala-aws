package com.codeminders.scalaws.s3

import com.codeminders.scalaws.http.HTTPClient
import com.codeminders.scalaws.s3.api.RichBucket
import com.codeminders.scalaws.s3.api.KeysStream
import org.apache.commons.io.IOUtils

package object model {
  
  implicit def string2Key(keyName: String): Key = {
    new Key(keyName)
  }
  
  implicit def string2Bucket(bucketName: String): Bucket = {
    new Bucket(bucketName)
  }
  
  implicit def string2S3ObjectBuilder(data: String): S3ObjectBuilder = {
    new S3ObjectBuilder(IOUtils.toInputStream(data), data.length())
  }
  
}