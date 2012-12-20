package com.codeminders.scalaws.s3.http

import java.io.InputStream
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import HTTPMethod._
import java.io.File
import scala.xml.XML
import java.io.ByteArrayOutputStream
import org.apache.commons.codec.binary.Base64
import java.util.UUID
import java.io.FileInputStream
import java.net.URL
import scala.xml.Node

trait HTTPClientMock extends HTTPClient {

  var testId = ""
  val testDataPath = new File("data")
  var requestsCounter = 1

  abstract override protected def invoke(method: HTTPMethod, request: Request)(content: Option[InputStream] = None, contentLength: Long = 0): Response = {
    val requestLog = new File(new File(testDataPath, Base64.encodeBase64String(testId.getBytes())), requestsCounter.toString())

    requestsCounter += 1

    if (!requestLog.getParentFile().exists()) {
      requestLog.getParentFile().mkdirs()
    }

    retrieve(requestLog, method, request) match {
      case None => {
        val requestCopy = request.copy()
        val response = super.invoke(method, request)(content, contentLength)
        save(requestLog, method, requestCopy, response)
      }
      case Some(r) => r
    }

  }

  private def save(f: File, method: HTTPMethod, request: Request, response: Response): Response = {
    f.delete()
    f.createNewFile()
    val (serializedResponse, newResponse) = serializeResponse(response)
    val data = <HTTPClientMockLogEntry>
                 { serializeHTTPMethod(method) }
                 { serializeRequest(request) }
                 { serializedResponse }
               </HTTPClientMockLogEntry>
    XML.save(f.getCanonicalPath(), data)
    newResponse
  }

  private def retrieve(f: File, method: HTTPMethod, request: Request): Option[Response] = {
    if (f.exists()) {
      val xml = XML.load(new FileInputStream(f))
      if (deserializeRequest(xml) == request && deserializeHTTPMethod(xml) == method) {
        Option(deserializeResponse(xml))
      } else {
        None
      }
    } else {
      None
    }
  }

  private def deserializeRequest(xml: Seq[Node]): Request = {
    val request = new Request(new URL(xml \\ "URL" text))
    (xml \\ "RequestHeaders" \ "Header").foreach {
      h =>
        request((h \ "@key").toString()) = h.text
    }
    request
  }

  private def serializeRequest(r: Request): Seq[Node] = {
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

  private def deserializeResponse(xml: Seq[Node]): Response = {
    val response = new Response((xml \\ "StatusCode" text).toInt, xml \\ "StatusText" text, xml \\ "Content" text match {
      case "" => None
      case s: String => Option(new ByteArrayInputStream(Base64.decodeBase64(s)))
    })
    for (h <- (xml \\ "ResponseHeaders" \ "Header")) {
      response((h \ "@key").toString()) = h.text
    }
    response
  }

  private def serializeResponse(r: Response): (Seq[Node], Response) = {
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
    (<Response>
       <StatusCode>{ r.statusCode }</StatusCode>
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

  private def deserializeHTTPMethod(xml: Seq[Node]): HTTPMethod = {
    HTTPMethod.withName(xml \\ "Method" text)
  }

  def serializeHTTPMethod(m: HTTPMethod): Seq[Node] = {
    <Method>{ m.toString() }</Method>
  }
}