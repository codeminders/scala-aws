package com.codeminders.scalaws.s3.http
import java.net.URL
import scala.collection._
import java.io.InputStream
import scala.io.Source

object HTTPMethod extends Enumeration {
  type HTTPMethod = Value
  val GET, POST, PUT, DELETE, HEAD = Value
}

class Request(val endPoint: URL) extends HTTPHeaders[Request] {

  override def equals(any: Any): Boolean = {
    if (any.isInstanceOf[Request]) {
      val that: Request = any.asInstanceOf[Request]
      that.endPoint == this.endPoint &&
        that._headers == this._headers
    } else {
      false
    }
  }
  override def hashCode(): Int = {
    41 * (
      41 + this.endPoint.hashCode()) + this._headers.hashCode()
  }

  def copy(): Request = {
    val requestCopy = new Request(endPoint)
    _headers.foreach {
      kv =>
        requestCopy.setHeader(kv._1, kv._2)
    }
    requestCopy
  }
}