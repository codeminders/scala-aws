package com.codeminders.scalaws.s3

import com.codeminders.scalaws.s3.model.Bucket
import com.codeminders.scalaws.s3.model.Key

package object api {
  
  implicit def richBucket2KeysTree(b: RichBucket): KeysStream = {
	  new KeysStream(b, b.list().keys)
  }
  
  implicit def richBucket2Bucket(b : RichBucket): Bucket = {
	  b.bucket
  }
  
  implicit def richKey2Key(k: RichKey): Key = {
    new Key(k.bucket, k.name, k.metadata)
  }

}