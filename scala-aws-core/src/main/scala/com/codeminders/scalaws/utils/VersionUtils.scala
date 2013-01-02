package com.codeminders.scalaws.utils

object VersionUtils {
  
  val minorVersion = 0
  
  val majorVersion = 1
  
  val releaseNumber = 0
  
  def fullVersion = "%d.%d-%d".format(majorVersion, minorVersion, releaseNumber)
  
}