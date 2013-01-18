package com.codeminders.scalaws.s3.api

import com.codeminders.scalaws.s3.model.MultipartUploadPart

class MultipartUploadParts private (parts: Seq[MultipartUploadPart], hasNext: Boolean, next: Int => (Seq[MultipartUploadPart], Boolean)) extends Stream[MultipartUploadPart] {

  override def tail: MultipartUploadParts = {
    if (parts.tail.isEmpty) {
      val (p, hn) = next(parts.last.partNumber)
      new MultipartUploadParts(p, hn, next)
    } else {
      new MultipartUploadParts(parts.tail, hasNext, next)
    }
  }

  override def isEmpty = parts.isEmpty && !hasNext

  override def head = {
    parts.head
  }

  override def tailDefined = !parts.tail.isEmpty && hasNext

}

object MultipartUploadParts {

  def apply(listParts: (Int) => (Seq[MultipartUploadPart], Boolean)): MultipartUploadParts = {
    val (parts, hasNext) = listParts(-1)
    new MultipartUploadParts(parts, hasNext, listParts)
  }
}