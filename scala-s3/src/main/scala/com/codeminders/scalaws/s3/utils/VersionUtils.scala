package com.codeminders.scalaws.s3.utils

object VersionUtils {
  
  val minorVersion = 0
  
  val majorVersion = 1
  
  val releaseNumber = 0
  
  def fullVersion = "%d.%d-%d".format(majorVersion, minorVersion, releaseNumber)
  
}