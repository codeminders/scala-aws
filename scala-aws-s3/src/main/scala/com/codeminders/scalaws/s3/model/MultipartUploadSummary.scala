package com.codeminders.scalaws.s3.model

import StorageClass._
import java.util.Date

trait MultipartUploadSummary {
  
  val initiator: Owner
  
  val owner: Owner
  
  val storageClass: StorageClass
  
  val initiated: Date

}