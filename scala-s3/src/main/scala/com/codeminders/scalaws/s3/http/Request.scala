package com.codeminders.scalaws.s3.http
import java.net.URL
import scala.collection._
import java.io.InputStream
import scala.io.Source

object HTTPMethod extends Enumeration {
  type HTTPMethod = Value
  val GET, POST, PUT, DELETE, HEAD  = Value
}

class Request(val endPoint: URL) extends HTTPHeaders[Request]