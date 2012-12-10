package com.codeminders.scalaws.s3.http
import java.io.InputStream
import scala.collection._

class Response(val statusCode: Int, val statusText: String, val content: InputStream) {
  
  private val headers = mutable.Map[String, String]()
  
  def header(key: String): Option[String] = {
    if(headers.contains(key)) Option(headers(key)) else None
  }
  
  def setHeader(key: String, value: String) = headers(key) = value

}