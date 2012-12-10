package com.codeminders.scalaws.s3

import java.io.InputStream
import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream

trait HTTPClientMock extends HTTPClient {
  
//  private val objects: Map[String, Map[String, String]] = new HashMap
//  
//  private val s3IndexGETRootRE = """/s3index/[?]prefix=[/]{0,1}.*""".r
//  private val s3IndexPUTGETObjectRE = """/s3index/([^?]*)""".r
//  
//  private val s3IndexGETDir1RE = """/s3index/[?]prefix=dir1/.*""".r
//  private val s3IndexGETDir2RE = """/s3index/[?]prefix=dir2/.*""".r
//  private val s3IndexGETDir3RE = """/s3index/[?]prefix=dir3/.*""".r
//  private val s3IndexGETDir4RE = """/s3index/[?]prefix=dir2/dir4/.*""".r
//  
//  override def processRequest[T](url: Request, handler: (InputStream, String) => T): T = url.method.toLowerCase() match {
//      case "get" => handler(processGet(url), "UTF-8")
//      case "put" => handler(processPut(url), "UTF-8")
//      case m => throw new UnsupportedOperationException(classOf[HTTPClientMock].getClass.getName + " doesn't support method " + m)
//  }
//  
//  def processGet(url: Request): InputStream = url.path match {    
//    case s3IndexGETDir1RE() => new ByteArrayInputStream(<ListBucketResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/"><Name>s3index</Name><Prefix>dir1/</Prefix><Marker></Marker><MaxKeys>1000</MaxKeys><Delimiter>/</Delimiter><IsTruncated>false</IsTruncated><Contents><Key>dir1/3</Key><LastModified>2012-09-21T15:57:40.000Z</LastModified><ETag>&quot;d41d8cd98f00b204e9800998ecf8427e&quot;</ETag><Size>0</Size><StorageClass>STANDARD</StorageClass></Contents></ListBucketResult>.toString().getBytes("UTF-8"))
//    case s3IndexGETDir4RE() => new ByteArrayInputStream(<ListBucketResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/"><Name>s3index</Name><Prefix>dir2/dir4/</Prefix><Marker></Marker><MaxKeys>1000</MaxKeys><Delimiter>/</Delimiter><IsTruncated>false</IsTruncated><Contents><Key>dir2/dir4/6</Key><LastModified>2012-09-21T15:57:41.000Z</LastModified><ETag>&quot;d41d8cd98f00b204e9800998ecf8427e&quot;</ETag><Size>0</Size><StorageClass>STANDARD</StorageClass></Contents></ListBucketResult>.toString().getBytes("UTF-8"))
//    case s3IndexGETDir2RE() => new ByteArrayInputStream(<ListBucketResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/"><Name>s3index</Name><Prefix>dir2/</Prefix><Marker></Marker><MaxKeys>1000</MaxKeys><Delimiter>/</Delimiter><IsTruncated>false</IsTruncated><Contents><Key>dir2/2</Key><LastModified>2012-09-21T15:57:41.000Z</LastModified><ETag>&quot;d41d8cd98f00b204e9800998ecf8427e&quot;</ETag><Size>0</Size><StorageClass>STANDARD</StorageClass></Contents><Contents><Key>dir2/4</Key><LastModified>2012-09-21T15:57:41.000Z</LastModified><ETag>&quot;d41d8cd98f00b204e9800998ecf8427e&quot;</ETag><Size>0</Size><StorageClass>STANDARD</StorageClass></Contents><CommonPrefixes><Prefix>dir2/dir4/</Prefix></CommonPrefixes></ListBucketResult>.toString().getBytes("UTF-8"))
//    case s3IndexGETDir3RE() => new ByteArrayInputStream(<ListBucketResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/"><Name>s3index</Name><Prefix>dir3/</Prefix><Marker></Marker><MaxKeys>1000</MaxKeys><Delimiter>/</Delimiter><IsTruncated>false</IsTruncated><Contents><Key>dir3/5</Key><LastModified>2012-09-21T15:57:41.000Z</LastModified><ETag>&quot;d41d8cd98f00b204e9800998ecf8427e&quot;</ETag><Size>0</Size><StorageClass>STANDARD</StorageClass></Contents></ListBucketResult>.toString().getBytes("UTF-8"))    
//    case s3IndexGETRootRE() => new ByteArrayInputStream(<ListBucketResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/"><Name>s3index</Name><Prefix></Prefix><Marker></Marker><MaxKeys>1000</MaxKeys><Delimiter>/</Delimiter><IsTruncated>false</IsTruncated><Contents><Key>1</Key><LastModified>2012-10-11T10:23:28.000Z</LastModified><ETag>&quot;2aed058259a2e51f2ca011242eed6132&quot;</ETag><Size>16</Size><StorageClass>STANDARD</StorageClass></Contents><CommonPrefixes><Prefix>dir1/</Prefix></CommonPrefixes><CommonPrefixes><Prefix>dir2/</Prefix></CommonPrefixes><CommonPrefixes><Prefix>dir3/</Prefix></CommonPrefixes></ListBucketResult>.toString.getBytes("UTF-8"))
//    case s3IndexPUTGETObjectRE(p) => getObject(p)
//    case p => throw new UnsupportedOperationException("I don't know how to handle path " + p)
//  }
//  
//  def processPut(url: Request): InputStream = url.path match {
//    case s3IndexPUTGETObjectRE(p) => new ByteArrayInputStream(putObject(p, url.body.get.getContent()).getBytes("UTF-8"))
//    case p => throw new UnsupportedOperationException("I don't know how to handle path " + p)
//  }
//  
//  def putObject(objectName: String, data: InputStream): String = {
//    objects.put(objectName, HashMap("data" -> IOUtils.toString(data, "UTF-8")))
//    ""
//  }
//  
//  def getObject(objectName: String): InputStream = {
//    if(objects.contains(objectName)){
//    	new ByteArrayInputStream(objects(objectName)("data").getBytes("UTF-8"))
//    } else {
//    	throw new IllegalArgumentException
//    }
//  }
}
