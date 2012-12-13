package com.codeminders.scalaws.s3.http
import java.net.URL
import scala.collection._
import java.io.InputStream
import scala.io.Source

object HTTPMethod extends Enumeration {
  type HTTPMethod = Value
  val GET, POST, PUT, DELETE, HEAD  = Value
}



class Request(val endPoint: URL) extends Traversable[(String, String)] {
  
  val headers = mutable.Map[String, String]()
  
  def header(key: String): Option[String] = {
    if(headers.contains(key)) Option(headers(key)) else None
  }
  
  def setHeader(key: String, value: String): Request = {
   headers(key) = value 
   this
  }
  
  def hasHeader(key: String) = headers.contains(key)
  
  def update(key: String, value: String): Unit = setHeader(key, value)
  
  def foreach[U](f: ((String, String)) => U) = {
    headers.foreach(f)
  }
  
}