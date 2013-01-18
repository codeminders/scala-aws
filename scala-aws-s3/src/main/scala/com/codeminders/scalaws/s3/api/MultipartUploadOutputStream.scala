package com.codeminders.scalaws.s3.api

import java.io.OutputStream
import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.s3.model.Key
import com.codeminders.scalaws.http.HTTPClient
import com.codeminders.scalaws.http.Response
import com.codeminders.scalaws.s3.Request
import scala.xml.XML
import com.codeminders.scalaws.helpers.io.EmptyInputStream
import com.codeminders.scalaws.AmazonClientException
import scala.collection.mutable.ListBuffer
import java.io.ByteArrayInputStream
import org.apache.commons.io.IOUtils
import com.codeminders.scalaws.s3.model.S3Object

class MultipartUploadOutputStream(upload: RichMultipartUpload) extends OutputStream {
  
  require(!upload.isCompleted, "Can not create %s from closed Multipart Upload".format(getClass().getName()))

  private val ioBuffer = Array.ofDim[Byte](1024 * 1024 * 5)

  private var currentBufferPos = 0

  private var currentPartNumber = 1
  
  override def write(b: Int) {
    if (currentBufferPos + 1 >= ioBuffer.size) flush()
    ioBuffer(currentBufferPos) = b.toByte
    currentBufferPos += 1
  }

  override def write(b: Array[Byte], off: Int, len: Int) {
    var bytesCopied = 0
    while (bytesCopied < len) {
      val l = math.min(ioBuffer.size - currentBufferPos, len - bytesCopied)
      System.arraycopy(b, off + bytesCopied, ioBuffer, currentBufferPos, l)
      bytesCopied += l
      currentBufferPos += l
      if (currentBufferPos >= ioBuffer.size - 1) flush()
    }
  }

  override def close() {
    flush()
    upload.complete
  }

  override def flush() {
    if(currentBufferPos > 0){
	    upload(currentPartNumber) = (new ByteArrayInputStream(ioBuffer, 0, currentBufferPos), currentBufferPos)
	    currentPartNumber += 1
	    currentBufferPos = 0
    }
  }
  
}