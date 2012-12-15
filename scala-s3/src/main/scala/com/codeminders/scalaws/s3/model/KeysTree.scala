package com.codeminders.scalaws.s3.model

import scala.xml._
import java.io.InputStream
import java.io.ByteArrayInputStream
import scala.io.Source
import com.codeminders.scalaws.s3.http.HTTPClient
import com.codeminders.scalaws.s3.http.Response
import java.net.URL
import com.codeminders.scalaws.s3.http.Request
import com.codeminders.scalaws.s3.model.Region._

trait KeysTree[T <: KeysTree[T]] extends Traversable[Key] {
  
  val prefix: String = ""
    
  val delimiter: String = "/"
  
  val self = this.asInstanceOf[T]
  
  lazy val (keys, commonPrefexes) = list(prefix, delimiter)
  
  lazy val keyGroups = commonPrefexes map { 
    (e => newInstance(e, delimiter)) 
  }
  
  lazy val keysNumber = keys.size
  
  def foreach[U](f: Key => U) = {
    keys.foreach(f)
    if (keyGroups.size > 0) {
      keyGroups.foreach(_.foreach(f))
    }
  }
  
  lazy val groupsNumber = keyGroups.size
  
  def refresh: T = {
    newInstance(prefix, delimiter)
  }
  
  def list(prefix: String = "", delimiter: String = "/", maxKeys: Int = 1000, marker: String = ""): (Array[Key], Array[String])
  
  protected def newInstance(prefix: String, delimiter: String): T
  
}