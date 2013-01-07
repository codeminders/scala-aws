package com.codeminders.scalaws.s3.examples

import com.codeminders.scalaws.s3.AWSS3
import com.codeminders.scalaws.AWSCredentials
import java.io.File
import com.codeminders.scalaws.s3.model.Key
import scala.annotation.tailrec
import java.util.Date

object Sync extends App {
  
  if(args.length != 2) {
    throw new IllegalArgumentException("Usage: " +  Sync.getClass().getName() + " local_path bucket_name")
  }
  
  val localPath = new File(args(0))
  val bucketName = args(1)
  
  if(!localPath.exists()){
    throw new IllegalArgumentException("There is no such file or directory as %s".format(localPath))
  }
  
  new Sync(localPath, bucketName).sync

}

class Sync(localPath: File, bucketName: String){
  
  val client: AWSS3 = AWSS3(AWSCredentials())
  
  if(!client.exist(bucketName)) client.create(bucketName)
  
  val bucket = client(bucketName)
  
  def sync(){
    syncRecursively(localPath)
  }
  
  @tailrec
  private def syncRecursively(dir: File){
    for(file <- dir.listFiles()){
      if(file.isDirectory()) syncRecursively(file)
      else {
        val key = file.getAbsolutePath().substring(localPath.getAbsolutePath().length())
        if(!bucket.exist(key)){
          bucket(key) = file
        } else {
          bucket(key).metadata.lastModified match {
            case None => bucket(key) = file
            case Some(d) => if(d.before(new Date(file.lastModified()))) bucket(key) = file
          }
        }
      }
    }
  }
  
}