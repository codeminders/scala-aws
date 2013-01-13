package com.codeminders.scalaws.http

import scala.collection._

trait HTTPHeaders[T <: HTTPHeaders[T]] extends Traversable[Tuple2[String, String]] {
  
  private val self = this.asInstanceOf[T]
  
  protected val _headers = mutable.Map.empty[String, String]
  
  def headers = immutable.Map(_headers.toSeq: _*)
  
  def header(key: String): Option[String] = {
    if(_headers.contains(key)) Option(_headers(key)) else None
  }
  
  def setHeader(key: String, value: String): T = {
   _headers(key) = value 
   self
  }
  
  def hasHeader(key: String) = _headers.contains(key)
  
  def update(key: String, value: String): Unit = setHeader(key, value)
  
  def apply(key: String): String = header(key).get
  
  def foreach[U](f: ((String, String)) => U) = {
    _headers.foreach(f)
  }

}