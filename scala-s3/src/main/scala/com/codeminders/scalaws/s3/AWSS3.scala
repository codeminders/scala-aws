package com.codeminders.scalaws.s3
import com.codeminders.scalaws.s3.http.HTTPClient
import com.codeminders.scalaws.s3.http.ClientConfiguration
import com.codeminders.scalaws.s3.http.HMACSingature
import com.codeminders.scalaws.s3.http.ApacheHTTPClient
import com.codeminders.scalaws.s3.model.Region._
import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.s3.api.RichBucket

class AWSS3(config: ClientConfiguration) extends ApacheHTTPClient(config){
  
  def bucket(name: String, region: Region = US_Standard): RichBucket = new RichBucket(this, new Bucket(name, region))
  
}

object AWSS3 {
  def apply(cred: AWSCredentials): AWSS3 = {
    new AWSS3(new ClientConfiguration()) with HMACSingature { this.credentials = cred }
  }

  def apply(): AWSS3 = {
    new AWSS3(new ClientConfiguration())
  }

}