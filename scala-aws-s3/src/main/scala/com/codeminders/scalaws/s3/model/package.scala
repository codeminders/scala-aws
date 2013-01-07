package com.codeminders.scalaws.s3

import com.codeminders.scalaws.http.HTTPClient
import com.codeminders.scalaws.s3.api.RichBucket
import com.codeminders.scalaws.s3.api.Keys
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.InputStream
import java.io.FileInputStream
import scala.io.Source

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
  
  implicit def file2S3ObjectBuilder(file: File): S3ObjectBuilder = {
    new S3ObjectBuilder(new FileInputStream(file), file.length())
  }
  
}