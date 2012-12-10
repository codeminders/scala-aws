package com.codeminders.scalaws.s3
import java.net.URL

object HTTPMethod extends Enumeration {
  type HTTPMethod = Value
  val GET, POST, PUT, DELETE, HEAD  = Value
}

import HTTPMethod._
import scala.collection._

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