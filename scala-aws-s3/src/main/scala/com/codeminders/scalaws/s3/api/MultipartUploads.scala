package com.codeminders.scalaws.s3.api

import com.codeminders.scalaws.s3.model.MultipartUpload
import com.codeminders.scalaws.s3.model.MultipartUploadSummary
import com.codeminders.scalaws.s3.model.Key
import scala.collection.mutable.Map

class MultipartUploads private (uploads: Seq[MultipartUpload with MultipartUploadSummary], prefexes: Seq[String], hasNext: Boolean, next: (String, String, String) => (Seq[MultipartUpload with MultipartUploadSummary], Seq[String], Boolean), val prefix: String) extends Stream[MultipartUpload with MultipartUploadSummary] {
  
  lazy val commonPrefexes: Seq[MultipartUploads] = {
    if (!prefexes.isEmpty) {
      for (prefix <- prefexes) yield {
        val (uplds, pfxs, hn) = next(prefix, "", "")
        new MultipartUploads(uplds, pfxs, hn, next, prefix)
      }
    } else Seq.empty
  }
  
  override def tail: MultipartUploads = {
    if (uploads.tail.isEmpty) {
      val (uplds, pfxs, hn) = next(prefix, uploads.last.key.name, uploads.last.uploadID)
      new MultipartUploads(uplds, pfxs, hn, next, prefix)
    } else {
      new MultipartUploads(uploads.tail, prefexes, hasNext, next, prefix)
    }
  }

  override def isEmpty = uploads.isEmpty && !hasNext

  override def head = {
    uploads.head
  }

  override def tailDefined = !uploads.tail.isEmpty && hasNext
  
}

object MultipartUploads {

  def apply(next: (String, String, String) => (Seq[MultipartUpload with MultipartUploadSummary], Seq[String], Boolean), prefix: String, keyMarker: String, uploadMarker: String): MultipartUploads = {
    val (uploads, prefexes, hasNext) = next(prefix, keyMarker, uploadMarker)
    new MultipartUploads(uploads, prefexes, hasNext, next, prefix)
  }
}