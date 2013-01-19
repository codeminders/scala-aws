package com.codeminders.scalaws.s3.model

import java.util.Date
import StorageClass._

abstract class S3ObjectSummary(val key: Key) {
  
  val size: Long
  
  val etag: String
  
  val lastModified: Date
  
  val storageClass: StorageClass
  
  val owner: Owner

}