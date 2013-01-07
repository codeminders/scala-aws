package com.codeminders.scalaws.s3.api

import com.codeminders.scalaws.http.HTTPClient
import com.codeminders.scalaws.http.Response
import com.codeminders.scalaws.s3.model.S3Object
import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.s3.model.ObjectMetadata
import com.codeminders.scalaws.s3.model.Expiration
import com.codeminders.scalaws.utils.DateUtils
import java.io.InputStream
import java.net.URL
import com.codeminders.scalaws.s3.Request

class RichS3Object(client: HTTPClient, val bucket: Bucket, val key: RichKey) {

  lazy val (content, contentLength) = {
    val req = Request(bucket.name, key.name)
    client.get(req, (r: Response) => {
      (r.content.get, r("Content-Length").toLong)
    })
  }
  
  def metadata = key.metadata

}