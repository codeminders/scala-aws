package com.codeminders.scalaws.s3

object Protocol extends Enumeration {
  type Protocol = Value
  val HTTP, HTTPS = Value
}

import Protocol._

class ClientConfiguration (
    var connectionTimeout: Int = 50 * 1000,
    var maxConnections: Int = 50,
    var maxErrorRetry: Int = 3,
    var protocol: Protocol = HTTP,
    var proxyDomain: Option[String] = None,
    var proxyWorkstation: Option[String] = None,
    var proxyHost: Option[String] = None,
    var proxyPassword: Option[String] = None,
    var proxyPort: Option[Int] = None,
    var proxyUsername: Option[String] = None,
    var socketTimeout: Int = 50 * 1000,
    var userAgent: String = "Scala AWS", //TODO: initialize this value from library version
    var bufferSize: Int = 0
    ){

}