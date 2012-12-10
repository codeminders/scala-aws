package com.codeminders.scalaws.s3.http
import java.io.InputStream
import HTTPMethod._

abstract class HTTPClient(protected val config: ClientConfiguration) {

  def get[T](r: Request, handler: (Response) => T): T = {
    handler(postProccess(invoke(HTTPMethod.GET, preProcess(HTTPMethod.GET, r))()))
  }
  
  def put[T](r: Request, handler: (Response) => T)(content: InputStream, contentLength: Long): T = {
    handler(postProccess(invoke(HTTPMethod.PUT, preProcess(HTTPMethod.PUT, r))(Option(content), contentLength)))
  }
  
  protected def preProcess(method: HTTPMethod, request: Request): Request = request
  
  protected def postProccess(response: Response): Response = response

  protected def invoke(method: HTTPMethod, request: Request)(content: Option[InputStream] = None, contentLength: Long = 0): Response = {
    throw new UnsupportedOperationException()
  }
  
}