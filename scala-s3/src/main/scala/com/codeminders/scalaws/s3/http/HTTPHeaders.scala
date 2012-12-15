package com.codeminders.scalaws.s3.http

import scala.collection._
import com.codeminders.scalaws.s3._

trait HTTPHeaders[T <: HTTPHeaders[T]] extends Traversable[Tuple2[String, String]] {
  
  val self = this.asInstanceOf[T]
  
  val headers = mutable.Map[String, String]()
  
  def header(key: String): Option[String] = {
    if(headers.contains(key)) Option(headers(key)) else None
  }
  
  def setHeader(key: String, value: String): T = {
   headers(key) = value 
   self
  }
  
  def hasHeader(key: String) = headers.contains(key)
  
  def update(key: String, value: String): Unit = setHeader(key, value)
  
  def foreach[U](f: ((String, String)) => U) = {
    headers.foreach(f)
  }

}