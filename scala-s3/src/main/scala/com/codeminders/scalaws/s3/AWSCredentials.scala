package com.codeminders.scalaws.s3

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Properties
import java.io.IOException
import scala.io.Source
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream

class AWSCredentials(val accessKeyId: String, val secretKey: String){
  if (accessKeyId.isEmpty()) throw AmazonClientException("AWS Access Key could not be empty")
  if (secretKey.isEmpty()) throw AmazonClientException("AWS Secret Key could not be empty")
}

object AWSCredentials {
  
  private val ACCESS_KEY_ENV_VAR: String = "AWS_ACCESS_KEY_ID"

  private val SECRET_KEY_ENV_VAR: String = "AWS_SECRET_KEY"
  
  def apply(): AWSCredentials = {
    if(!System.getenv().containsKey(ACCESS_KEY_ENV_VAR) && System.getProperty(ACCESS_KEY_ENV_VAR) == null) 
      throw AmazonClientException("Please set either environment variable or Java property with name " + ACCESS_KEY_ENV_VAR)
    if(!System.getenv().containsKey(SECRET_KEY_ENV_VAR) && System.getProperty(SECRET_KEY_ENV_VAR) == null) 
      throw AmazonClientException("Please set either environment variable or Java property with name " + SECRET_KEY_ENV_VAR)
    apply(System.getProperty(ACCESS_KEY_ENV_VAR, System.getenv().get(ACCESS_KEY_ENV_VAR)), System.getProperty(SECRET_KEY_ENV_VAR, System.getenv().get(SECRET_KEY_ENV_VAR)))
  }
  
  def apply(accessKeyId: String, secretKey: String): AWSCredentials = {
    new AWSCredentials(accessKeyId.trim(), secretKey.trim())
  }
  
  def apply(f: File): AWSCredentials = {
    apply(new FileInputStream(f))
  }
  
  def apply(s: Source): AWSCredentials = {
    apply(new ByteArrayInputStream(s.toStream.takeWhile(-1 !=).map(_.toByte).toArray))
  }

  def apply(is: InputStream): AWSCredentials = {

    val properties = new Properties()

    try {
      properties.load(is)
    } catch {
      case e: IOException => throw AmazonClientException(e)
    }

    apply(properties)
  }
  
  def apply(properties: Properties): AWSCredentials = {

    val accessKey = 
      if (properties.getProperty("accessKey", "").isEmpty()) throw AmazonClientException("The source with AWS credentials should contain 'accessKey=' parameter") 
      else properties.getProperty("accessKey")
    val secretKey = 
      if (properties.getProperty("secretKey", "").isEmpty()) throw AmazonClientException("The source with AWS credentials should contain 'secretKey=' parameter") 
      else properties.getProperty("secretKey")

    apply(accessKey, secretKey)
  }
}