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

abstract class KeysTree(client: HTTPClient, prefix: String = "", delimiter: String = "/") extends Traversable[Key] {
  
  lazy val (keys, commonPrefexes) = list(prefix, delimiter)
  
  lazy val keyGroups = commonPrefexes map { 
    (e => newInstance(client, e, delimiter)) 
  }
  
  lazy val keysNumber = keys.size
  
  def foreach[U](f: Key => U) = {
    keys.foreach(f)
    if (keyGroups.size > 0) {
      keyGroups.foreach(_.foreach(f))
    }
  }
  
  lazy val groupsNumber = keyGroups.size
  
  def list(prefix: String = "", delimiter: String = "/", maxKeys: Int = 1000, marker: String = ""): (Array[Key], Array[String])
  
  protected def newInstance(client: HTTPClient, prefix: String, delimiter: String): KeysTree
  
}