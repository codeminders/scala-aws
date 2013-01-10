package com.codeminders.scalaws.s3.model

import java.util.Date
import scala.collection.immutable.Map

class Expiration(val expiryDate: Date, val ruleId: String)

class ObjectMetadata(val size: Option[Long] = None,
  val contentMD5: Option[String] = None,
  val lastModified: Option[Date] = None,
  val expiration: Option[Expiration] = None,
  val contentType: Option[String] = None,
  val versionId: Option[String] = None,
  val userMetadata: Map[String, String] = Map.empty)
