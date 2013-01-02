package com.codeminders.scalaws.s3.model

import java.util.Date
import scala.collection.immutable.Map

object StorageClass extends Enumeration {
  type StorageClass = Value
  val STANDARD, REDUCED_REDUNDANCY = Value
}

import StorageClass._

class Expiration(val expiryDate: Date, val ruleId: String)

class ObjectMetadata(val size: Option[Long] = None,
  val contentMD5: Option[String] = None,
  val lastModified: Option[Date] = None,
  val storageClass: Option[StorageClass] = None,
  val owner: Option[Owner] = None,
  val expiration: Option[Expiration] = None,
  val contentType: Option[String] = None,
  val versionId: Option[String] = None,
  val userMetadata: Map[String, String] = Map.empty)

object ObjectMetadata{
  def apply(size: Long, md5Sum: String, lastModified: Date, storageClass: StorageClass, owner: Owner): ObjectMetadata = {
    new ObjectMetadata(Option(size), Option(md5Sum), Option(lastModified), Option(storageClass), Option(owner))
  }
}