package com.codeminders.scalaaws.examples

import com.codeminders.scalaws
import com.codeminders.scalaws.s3.AWSS3
import com.codeminders.scalaws.s3.model.Key
import com.codeminders.scalaws.s3.api.KeysStream
import com.codeminders.scalaws.s3.model.ObjectMetadata
import com.codeminders.scalaws.AWSCredentials

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

object PrintKeysTree extends App {    
  
  if(args.length != 1) {
    throw new IllegalArgumentException("You forgot to specify a bucket")
  }
  
  val bucketName = args(0)
  
  val client: AWSS3 = AWSS3(AWSCredentials())
  
  if(client.exist(bucketName)){
    outputKeysTree(client(bucketName).list(delimiter="/"))
  } else {
    println("There is no such bucket: %s".format(bucketName))
  }
  
  def outputKeysTree(stream: KeysStream, prefix: String = ""){
    stream.commonPrefexes.foreach {
      s =>
        println(prefix + "|-" + stream.prefix) 
        outputKeysTree(s, (0 to prefix.length).foldLeft("") {(s, c) => s + " "} + " ")
    }
    stream.foreach{
      k =>
        println(prefix + "|-" + k.name)
    }
  }

}