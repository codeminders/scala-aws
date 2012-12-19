package com.codeminders.scalaws.s3

import com.codeminders.scalaws.s3.http.HTTPClient
import com.codeminders.scalaws.s3.api.RichBucket
import com.codeminders.scalaws.s3.api.KeysStream
import com.codeminders.scalaws.s3.api.RichKey

package object model {
  
  implicit def bucket2KeysTree(b : Bucket)(implicit client: HTTPClient): KeysStream = {
	  val richBucket = new RichBucket(client, b)
	  new KeysStream(richBucket, richBucket.list().keys)
  }
  
  implicit def bucket2RichBucket(b : Bucket)(implicit client: HTTPClient): RichBucket = {
	  new RichBucket(client, b)
  }
  
  implicit def key2RichKey(k: Key)(implicit client: HTTPClient): RichKey = {
    new RichKey(client, k)
  }


}