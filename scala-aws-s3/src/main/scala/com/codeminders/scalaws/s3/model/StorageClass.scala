package com.codeminders.scalaws.s3.model

object StorageClass extends Enumeration {
  type StorageClass = Value
  val STANDARD, REDUCED_REDUNDANCY = Value
}