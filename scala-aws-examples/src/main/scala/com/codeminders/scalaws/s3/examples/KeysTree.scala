package com.codeminders.scalaws.s3.examples

import com.codeminders.scalaws
import com.codeminders.scalaws.s3.AWSS3
import com.codeminders.scalaws.s3.model.Key
import com.codeminders.scalaws.s3.api.Keys
import com.codeminders.scalaws.s3.model.ObjectMetadata
import com.codeminders.scalaws.AWSCredentials
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.s3.AmazonS3Client

/*
 *   /
 *   |-A
 *   | |-B1
 *   | | |-1
 *   | | |-2
 *   | |
 *   | |-B2
 *   |    |-1
 *   |-C
 *     |-1
 *     
 */

object KeysTree extends App {    
  
  if(args.length > 2 || args.length < 1) {
    throw new IllegalArgumentException("Usage: " +  KeysTree.getClass().getName() + " bucket [delimiter]")
  }
  
  val bucketName = args(0)
  
  val delimiter = if(args.length > 1){
    args(1)
  } else "/"
  
  println("/")
  new KeysTree(bucketName, delimiter).printTree()
  
}

class KeysTree(bucketName: String, delimiter: String = "/") {
  
  val client: AWSS3 = AWSS3(AWSCredentials())
  
  def printTree(stream:Keys = client(bucketName).list(delimiter=delimiter), prefix: String = "") {
    stream.commonPrefexes.foreach {
      s =>
        println(prefix + "|-" + s.prefix.substring(stream.prefix.length()))
        if(stream.isEmpty)
        	printTree(s, prefix + "  ")
        else
          printTree(s, prefix + "| ")
    }
    stream.foreach{
      k =>
        println(prefix + "|-" + k.name.substring(stream.prefix.length()))
    }
    if(!stream.isEmpty) println(prefix)
  }
  
}