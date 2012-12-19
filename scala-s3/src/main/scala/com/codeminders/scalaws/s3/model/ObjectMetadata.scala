package com.codeminders.scalaws.s3.model
import java.util.Date
import scala.collection.immutable.Map

class ObjectMetadata (
  
  val contentType: Option[String] = None,
  val expires: Option[Long] = None,
  val size: Option[Long] = None,
  val contentMD5: Option[String] = None,
  val lastModified: Option[Date] = None,
  val userMetadata: Map[String, String] = Map()

)