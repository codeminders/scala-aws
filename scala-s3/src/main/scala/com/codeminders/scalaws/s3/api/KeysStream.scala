package com.codeminders.scalaws.s3.api

import scala.xml._
import java.io.InputStream
import java.io.ByteArrayInputStream
import scala.io.Source
import com.codeminders.scalaws.s3.http.HTTPClient
import com.codeminders.scalaws.s3.http.Response
import java.net.URL
import java.util.Date

import com.codeminders.scalaws.s3.http.Request
import com.codeminders.scalaws.s3.model.Region._
import com.codeminders.scalaws.s3.model.Key
import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.s3.AmazonClientException
import com.codeminders.scalaws.s3.DateUtils

class KeysStream(bucket: RichBucket, keys: Seq[Key] = Seq.empty, isTruncated: Boolean = false, marker: String = "") extends Stream[Key] {
  
  override def tail: Stream[Key] = {
    if (keys.isEmpty) {
        val listBucketResult = bucket.list(marker = marker)
        new KeysStream(bucket, listBucketResult.keys, listBucketResult.isTruncated, listBucketResult.keys.last.name)
      } else {
      new KeysStream(bucket, keys.tail, isTruncated, marker)
    }
  }

  override def isEmpty = keys.isEmpty && !isTruncated

  override def head = {
    keys.head
  }

  protected def tailDefined = !keys.tail.isEmpty && isTruncated

}