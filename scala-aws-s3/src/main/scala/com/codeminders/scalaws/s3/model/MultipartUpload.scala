package com.codeminders.scalaws.s3.model

class MultipartUpload(val bucket: Bucket, val key: Key, val uploadID: String){
  
  override def equals(any: Any): Boolean = {
    if (any.isInstanceOf[MultipartUpload]) {
      val that = any.asInstanceOf[MultipartUpload]
      that.uploadID.equals(this.uploadID)
    } else {
      false
    }
  }

  override def hashCode(): Int = {
    this.uploadID.hashCode()
  }

  override def toString: String = {
    "MultipartUpload[bucket=%s,key=%s,uploadID=%s]".format(bucket.name, key.name, uploadID)
  }

}