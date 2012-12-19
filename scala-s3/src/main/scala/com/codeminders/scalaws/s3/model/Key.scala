package com.codeminders.scalaws.s3.model

import java.io.InputStream
import java.io.OutputStream
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import scala.io.Source
import com.codeminders.scalaws.s3.http.HTTPClient
import com.codeminders.scalaws.s3.http.Request
import java.net.URL
import java.io.BufferedInputStream
import com.codeminders.scalaws.s3.http.Response
import scala.collection.immutable.Map
import scala.collection._
import com.codeminders.scalaws.s3.AmazonClientException
import java.util.Date
import com.codeminders.scalaws.s3.DateUtils

object StorageClass extends Enumeration {
  type StorageClass = Value
  val STANDARD, REDUCED_REDUNDANCY = Value
}

class Key(val bucket: Bucket, val name: String, val metadata: ObjectMetadata = new ObjectMetadata()) {

  override def toString() = "Key[/%s/%s]".format(bucket.name, name)

}