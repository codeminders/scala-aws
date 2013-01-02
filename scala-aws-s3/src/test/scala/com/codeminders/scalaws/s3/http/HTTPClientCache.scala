package com.codeminders.scalaaws.s3.http

import java.io.InputStream
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import java.io.File
import scala.xml.XML
import java.io.ByteArrayOutputStream
import org.apache.commons.codec.binary.Base64
import java.util.UUID
import java.io.FileInputStream
import java.net.URL
import scala.xml.Node
import scala.io.Source
import com.codeminders.scalaws.utils.VersionUtils
import com.codeminders.scalaws.AmazonServiceException
import com.codeminders.scalaws.http.Response
import com.codeminders.scalaws.http.HTTPClient
import com.codeminders.scalaws.http.HTTPMethod
import com.codeminders.scalaws.http.Request
import com.codeminders.scalaws.http.Response

import HTTPMethod._

trait HTTPClientCache extends HTTPClient {

  val HTTPClientCacheVersion = "1.0"

  val CLEAR_HTTP_CLIENT_CACHE = "CLEAR_HTTP_CLIENT_CACHE"

  val cacheId: String = "all"
  val cacheData = ".httpClientCache"
  var requestsCounter = 1
  var sequenceIsSkewed = false

  abstract override protected def invoke(method: HTTPMethod, request: Request)(content: Option[InputStream] = None, contentLength: Long = 0): Response = {
    val requestLog = new File(new File(cacheData, Base64.encodeBase64String(cacheId.getBytes())), requestsCounter.toString())

    requestsCounter += 1

    if (!requestLog.getParentFile().exists()) {
      requestLog.getParentFile().mkdirs()
    }

    retrieve(requestLog, method, request) match {
      case None => {
        val requestCopy = request.copy()
        try {
          val response = super.invoke(method, request)(content, contentLength)
          save(requestLog, method, requestCopy, response)
        } catch {
          case ase: AmazonServiceException => {
            save(requestLog, method, requestCopy, ase)
            throw ase
          }
          case other => throw other
        }
      }
      case Some(r) => r
    }

  }

  private lazy val clearHttpClientCache = {
    if (!System.getenv().containsKey(CLEAR_HTTP_CLIENT_CACHE) && System.getProperty(CLEAR_HTTP_CLIENT_CACHE) == null) {
      false
    } else {
      System.getProperty(CLEAR_HTTP_CLIENT_CACHE, System.getenv().get(CLEAR_HTTP_CLIENT_CACHE)).toBoolean
    }
  }

  private def save(f: File, method: HTTPMethod, request: Request, e: AmazonServiceException): Unit = {
    val data = serializeHTTPMethod(method) ++
      serializeRequest(request) ++ serializeError(e)
    save(f, data)
  }

  private def save(f: File, method: HTTPMethod, request: Request, response: Response): Response = {
    val (serializedResponse, newResponse) = serializeResponse(response)
    val data = serializeHTTPMethod(method) ++
      serializeRequest(request) ++
      serializedResponse
    save(f, data)
    newResponse
  }

  private def save(f: File, xml: Seq[Node]): Unit = {
    f.delete()
    f.createNewFile()
    XML.save(f.getCanonicalPath(), <HTTPClientCacheEntry version={ HTTPClientCacheVersion }>{ xml }</HTTPClientCacheEntry>)
  }

  private def retrieve(f: File, method: HTTPMethod, request: Request): Option[Response] = {
    if (f.exists()) {
      val xml = XML.load(new FileInputStream(f))
      if (clearHttpClientCache || deserializedVersion(xml) != HTTPClientCacheVersion || deserializedHTTPMethod(xml) != method || deserializedRequest(xml) != request) {
        sequenceIsSkewed = true
      }
      if (!sequenceIsSkewed) {
        Option(deserializedResponse(xml))
      } else {
        None
      }
    } else {
      None
    }
  }

  private def deserializedVersion(xml: Node): String = {
    xml \\ "HTTPClientCacheEntry" \ "@version" text
  }

  private def deserializedRequest(xml: Node): Request = {
    val request = new Request(new URL(xml \\ "URL" text))
    (xml \\ "RequestHeaders" \ "Header").foreach {
      h =>
        request((h \ "@key").toString()) = h.text
    }
    request
  }

  private def serializeRequest(r: Request): Node = {
    <Request>
      <URL>{ r.endPoint.toString() }</URL>
      <RequestHeaders>
        {
          for (kv <- r) yield {
            <Header key={ kv._1 }>{ kv._2 }</Header>
          }
        }
      </RequestHeaders>
    </Request>
  }

  private def deserializedResponse(xml: Node): Response = {
    val responseType = (xml \\ "Response" \ "@type").text
    responseType match {
      case "error" => throw AmazonServiceException((xml \\ "Response" \ "@statusCode" text).toInt, (xml \\ "Response" \ "Error"))
      case "response" => {
        val response = new Response((xml \\ "Response" \ "@statusCode" text).toInt, xml \\ "StatusText" text, xml \\ "Content" text match {
          case "" => None
          case s: String => Option(new ByteArrayInputStream(Base64.decodeBase64(s)))
        })
        for (h <- (xml \\ "ResponseHeaders" \ "Header")) {
          response((h \ "@key").toString()) = h.text
        }
        response
      }
    }
  }

  private def serializeResponse(r: Response): (Node, Response) = {
    val responseData = r.content match {
      case None => None
      case Some(is) => Option(IOUtils.toByteArray(is))
    }
    val stream = responseData match {
      case None => None
      case Some(d) => Option(new ByteArrayInputStream(d))
    }
    val newResponse = new Response(r.statusCode, r.statusText, stream)
    r.headers.foreach {
      kv =>
        newResponse.setHeader(kv._1, kv._2)
    }
    (<Response type="response" statusCode={ r.statusCode.toString() }>
       <StatusText>{ r.statusText }</StatusText>
       <ResponseHeaders>
         {
           for (kv <- r) yield {
             <Header key={ kv._1 }>{ kv._2 }</Header>
           }
         }
       </ResponseHeaders>
       {
         responseData match {
           case None => <Content/>
           case Some(d) => <Content>{ Base64.encodeBase64String(d) }</Content>
         }
       }
     </Response>, newResponse)
  }
  
  private def serializeError(e: AmazonServiceException): Node = {
    <Response type="error" statusCode={ e.statusCode.toString() }>
	  {e.xml}
	</Response>
  }

  private def deserializedHTTPMethod(xml: Node): HTTPMethod = {
    HTTPMethod.withName(xml \\ "Method" text)
  }

  private def serializeHTTPMethod(m: HTTPMethod): Node = {
    <Method>{ m.toString() }</Method>
  }

}