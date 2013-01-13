package com.codeminders.scalaws.s3.model

object ServerSideEncryption extends Enumeration {
  type ServerSideEncryption = Value
  val AES256 = Value
}