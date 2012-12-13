package com.codeminders.scalaws.s3
import com.codeminders.scalaws.s3.http.HTTPClient
import com.codeminders.scalaws.s3.http.ClientConfiguration
import com.codeminders.scalaws.s3.http.HMACSingature
import com.codeminders.scalaws.s3.http.ApacheHTTPClient
import com.codeminders.scalaws.s3.model.Region._
import com.codeminders.scalaws.s3.model.Bucket

class AWSS3(httpClient: HTTPClient){
  
  def bucket(name: String, region: Region = US_Standard): Bucket = new Bucket(httpClient, name)
  
}

object AWSS3 {
  def apply(cred: AWSCredentials): AWSS3 = {
    new AWSS3(new ApacheHTTPClient(new ClientConfiguration()) with HMACSingature { this.credentials = cred })
  }

  def apply(): AWSS3 = {
    new AWSS3(new ApacheHTTPClient(new ClientConfiguration()))
  }

}