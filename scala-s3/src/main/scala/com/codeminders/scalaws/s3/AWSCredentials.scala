package com.codeminders.scalaws.s3

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Properties
import java.io.IOException

class AWSCredentials(val accessKeyId: String, val secretKey: String) {
}

object AWSCredentials {
  def apply(accessKeyId: String, secretKey: String): AWSCredentials = {
    if (accessKeyId.isEmpty() || secretKey.isEmpty()) throw AmazonClientException("Arguments to AWSCredentials constructor could not be empty strings")
    new AWSCredentials(accessKeyId, secretKey)
  }

  def apply(is: InputStream): AWSCredentials = {

    val properties = new Properties()

    try {
      properties.load(is)
    } catch {
      case e: IOException => throw AmazonClientException(e)
    }

    val accessKey = 
      if (properties.getProperty("accessKey", "").isEmpty()) throw AmazonClientException("The file with AWS credentials should contain 'accessKey=' parameter") 
      else properties.getProperty("accessKey")
    val secretKey = 
      if (properties.getProperty("secretKey", "").isEmpty()) throw AmazonClientException("The file with AWS credentials should contain 'secretKey=' parameter") 
      else properties.getProperty("secretKey")

    AWSCredentials(accessKey, secretKey)
  }
}