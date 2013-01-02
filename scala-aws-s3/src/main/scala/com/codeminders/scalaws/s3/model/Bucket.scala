package com.codeminders.scalaws.s3.model

import scala.xml._
import java.io.InputStream
import java.io.ByteArrayInputStream
import scala.io.Source
import com.codeminders.scalaws.http.HTTPClient
import com.codeminders.scalaws.http.Response
import java.net.URL
import com.codeminders.scalaws.http.Request
import sun.security.util.Length
import com.codeminders.scalaws.AmazonClientException

object Region extends Enumeration {
  type Region = Value
  val US_Standard = Value("")
  val US_West = Value("us-west-1")
  val US_West_2 = Value("us-west-2")
  val EU_Ireland = Value("EU")
  val AP_Singapore = Value("ap-southeast-1")
  val AP_Tokyo = Value("ap-northeast-1")
  val SA_SaoPaulo = Value("sa-east-1")
}

import Region._

class Bucket(val name: String, val region: Region = Region.US_Standard){
  override def toString() = "Bucket[%s(%s)]".format(name, region.toString())
}
