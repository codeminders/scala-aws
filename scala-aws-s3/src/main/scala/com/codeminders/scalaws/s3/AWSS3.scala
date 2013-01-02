package com.codeminders.scalaws.s3

import com.codeminders.scalaws.http.HTTPClient
import com.codeminders.scalaws.http.ClientConfiguration
import com.codeminders.scalaws.http.HMACSingature
import com.codeminders.scalaws.http.ApacheHTTPClient
import com.codeminders.scalaws.AWSCredentials
import com.codeminders.scalaws.s3.model.Region._
import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.s3.api.RichBucket
import com.codeminders.scalaws.http.Request
import com.codeminders.scalaws.http.Response

import java.net.URL
import java.io.ByteArrayInputStream

class AWSS3(config: ClientConfiguration) extends ApacheHTTPClient(config){
  
  def delete(bucket: Bucket): Unit = {
    delete(new Request(new URL("http://%s.s3.amazonaws.com".format(bucket.name))), (r: Response) => None)
  }
  
  def create(bucket: Bucket, region: Region = US_Standard): RichBucket = {
    val req = new Request(new URL("http://%s.s3.amazonaws.com".format(bucket.name)))
    val data = region match {
      case US_Standard => {
        Array[Byte]()
      }
      case _ =>
        {
          (<CreateBucketConfiguration xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
             <LocationConstraint>{ region.toString() }</LocationConstraint>
           </CreateBucketConfiguration>).toString().getBytes()
        }
    }
    put(req, (r: Response) => None)(new ByteArrayInputStream(data), data.length)
    
    new RichBucket(this, bucket)
  }
  
  def apply(bucket: Bucket): RichBucket = {
    new RichBucket(this, bucket)
  }
  
}

object AWSS3 {
  def apply(cred: AWSCredentials): AWSS3 = {
    new AWSS3(new ClientConfiguration()) with HMACSingature { this.credentials = cred }
  }

  def apply(): AWSS3 = {
    new AWSS3(new ClientConfiguration())
  }

}