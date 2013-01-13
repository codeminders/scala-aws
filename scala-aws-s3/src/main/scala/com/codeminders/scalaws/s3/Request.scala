package com.codeminders.scalaws.s3

import java.net.URL
import scala.collection.mutable.ListBuffer
import com.codeminders.scalaws.utils.StringUtils
import com.codeminders.scalaws.AmazonClientException

object Request {

  def apply(bucketName: String = "", keyName: String = "", parameters: Seq[(String, String)] = Seq.empty, headers: Seq[(String, String)] = Seq.empty): com.codeminders.scalaws.http.Request = {

    if (bucketName.isEmpty()) {
      new com.codeminders.scalaws.http.Request(new URL("http://s3.amazonaws.com/"), headers)
    } else {
      val normalizedBucketName = normalizeBucketName(bucketName)
      val normalizedKeyName = normalizeKeyName(keyName)

      new com.codeminders.scalaws.http.Request(new URL(
        "http://%s.s3.amazonaws.com/%s".format(normalizedBucketName, normalizedKeyName) + (
          if (parameters.isEmpty) ""
          else
            "?" + parameters.foldLeft(ListBuffer[String]()) {
              (a, kv) => a += (kv._1 + "=" + kv._2)
            }.mkString("&"))), headers)
    }
  }

  private def normalizeBucketName(bucketName: String): String = {
    bucketName
  }

  private def normalizeKeyName(keyName: String): String = {
    StringUtils.removeCharacterDuplicates(keyName.dropWhile(_ == '/'), '/')
  }

}