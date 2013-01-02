package com.codeminders.scalaws.http
import java.io.InputStream
import scala.collection._

class Response(val statusCode: Int, val statusText: String, val content: Option[InputStream]) extends HTTPHeaders[Response]  {
  
}