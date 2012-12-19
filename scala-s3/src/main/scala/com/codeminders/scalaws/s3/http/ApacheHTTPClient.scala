package com.codeminders.scalaws.s3.http

import org.apache.http.client.HttpClient
import org.apache.http.params.HttpParams
import org.apache.http.params.BasicHttpParams
import org.apache.http.params.HttpConnectionParams
import org.apache.http.params.HttpProtocolParams
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.conn.scheme.Scheme
import javax.net.ssl.SSLContext
import org.apache.http.conn.scheme.PlainSocketFactory
import org.apache.http.conn.scheme.SchemeRegistry
import java.security.NoSuchAlgorithmException
import org.apache.http.HttpHost
import org.apache.http.conn.params.ConnRoutePNames
import org.apache.http.auth.AuthScope
import HTTPMethod._
import org.apache.http.auth.NTCredentials
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpHead
import org.apache.http.entity.InputStreamEntity
import java.io.InputStream
import org.apache.http.client.methods.HttpPut
import org.apache.http.HttpStatus
import scala.xml.XML
import com.codeminders.scalaws.s3.AmazonClientException
import com.codeminders.scalaws.s3.AmazonServiceException
import org.apache.http.client.methods.HttpDelete

class ApacheHTTPClient(config: ClientConfiguration) extends HTTPClient(config) {
  
  private val httpClient: HttpClient = {
    val httpClientParams: HttpParams = new BasicHttpParams();
    HttpProtocolParams.setUserAgent(httpClientParams, config.userAgent);
    HttpConnectionParams.setConnectionTimeout(httpClientParams, config.connectionTimeout);
    HttpConnectionParams.setSoTimeout(httpClientParams, config.socketTimeout);
    HttpConnectionParams.setStaleCheckingEnabled(httpClientParams, true);
    HttpConnectionParams.setTcpNoDelay(httpClientParams, true);

    if (config.bufferSize > 0) {
      HttpConnectionParams.setSocketBufferSize(httpClientParams, math.max(config.bufferSize, 1024 * 8))
    }

    val connectionManager: ThreadSafeClientConnManager = new ThreadSafeClientConnManager();
    connectionManager.setDefaultMaxPerRoute(config.maxConnections);
    connectionManager.setMaxTotal(config.maxConnections);

    val httpClient: DefaultHttpClient = new DefaultHttpClient(connectionManager, httpClientParams);

    try {
      val http: Scheme = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());

      val sf: SSLSocketFactory = new SSLSocketFactory(
        SSLContext.getDefault(),
        SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
      val https: Scheme = new Scheme("https", 443, sf);

      val sr: SchemeRegistry = connectionManager.getSchemeRegistry();
      sr.register(http);
      sr.register(https);
    } catch {
      case e: NoSuchAlgorithmException =>
        throw AmazonClientException("Unable to access default SSL context");
    }

    /* Set proxy if configured */
    if (config.proxyHost != None && config.proxyPort != None) {
      val proxyHttpHost: HttpHost = new HttpHost(config.proxyHost.get, config.proxyPort.get);
      httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHttpHost);

      if (config.proxyUsername != None && config.proxyPassword != None) {
        httpClient.getCredentialsProvider().setCredentials(
          new AuthScope(config.proxyHost.get, config.proxyPort.get),
          new NTCredentials(config.proxyUsername.get, config.proxyPassword.get, config.proxyWorkstation.get, config.proxyDomain.get));
      }
    }
    httpClient;
  }
  
  override protected def preProcess(method: HTTPMethod, request: Request): Request = {
    if(!request.hasHeader("Content-Type")){
      request.setHeader("Content-Type", "application/octet-stream")
    }
    request
  }
  
  override protected def invoke(method: HTTPMethod, request: Request)(content: Option[InputStream] = None, contentLength: Long = 0): Response = {
    val httpRequest = method match {
      case HTTPMethod.POST => {
        val r = new HttpPost(request.endPoint.toString())
        if(content != None) r.setEntity(new InputStreamEntity(content.get, contentLength))
        r
      }
      case HTTPMethod.GET => new HttpGet(request.endPoint.toString())
      case HTTPMethod.HEAD => new HttpHead(request.endPoint.toString())
      case HTTPMethod.DELETE => new HttpDelete(request.endPoint.toString())
      case HTTPMethod.PUT => {
        val r = new HttpPut(request.endPoint.toString())
        if(content != None) r.setEntity(new InputStreamEntity(content.get, contentLength))
        r 
      }
    }
    request.foreach(h => httpRequest.setHeader(h._1, h._2))
    val httpClientResponse = httpClient.execute(httpRequest)
    val responseContent = if(httpClientResponse.getEntity() == null) None else Option(httpClientResponse.getEntity().getContent())
    val response = new Response(httpClientResponse.getStatusLine().getStatusCode(), httpClientResponse.getStatusLine().getReasonPhrase(), responseContent)
    httpClientResponse.getAllHeaders().foreach(h => response.setHeader(h.getName(), h.getValue()))
    if(response.statusCode / 100 == HttpStatus.SC_OK / 100) {
      response
    } else {
      response.content match {
        case None => method match {
          //Fix to https://forums.aws.amazon.com/message.jspa?messageID=40931&tstart=0
          case HTTPMethod.HEAD => invoke(HTTPMethod.GET, request)()
          case _ => throw AmazonClientException("Error: %d: %s".format(response.statusCode, response.statusText)) 
        }
        case Some(is) => throw AmazonServiceException(response.statusCode, XML.load(is))
      }
      
    }
    
  }

}