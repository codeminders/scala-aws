package com.codeminders.scalaws.http

import scala.collection._
import scala.collection.mutable.Set

trait HTTPHeaders[T <: HTTPHeaders[T]] extends Traversable[Tuple2[String, String]] {
  
  private val self = this.asInstanceOf[T]
  
  protected val _headers: Set[(String, String)] = Set.empty
  
  def headers = _headers.toList
  
  def update(key: String, value: String) {
    _headers += Tuple2(key, value)
  }
  
  def exists(key: String): Boolean = {
    _headers.find(kv => kv._1 == key) match {
      case None => false
      case Some(v) => true
    }
  }
  
  def apply(key: String): Option[String] = _headers.find(kv => kv._1 == key) match {
    case None => None
    case Some(kv) => Some(kv._2)
  }
  
  def foreach[U](f: ((String, String)) => U) = {
    _headers.foreach(f)
  }

}