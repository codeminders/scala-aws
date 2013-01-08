package com.codeminders.scalaws.s3.examples

import com.codeminders.scalaws.s3.AWSS3
import com.codeminders.scalaws.AWSCredentials
import java.io.File
import com.codeminders.scalaws.s3.model.Key
import scala.annotation.tailrec
import java.util.Date

/**
 * Sync program entry point
 *
 * Usage:
 * <pre>
 * {@literal
 * Sync <i>local_path bucket_name</i>
 * }
 * </pre>
 */

object Sync extends App {

  if (args.length != 2) {
    throw new IllegalArgumentException("Usage: " + Sync.getClass().getName() + " local_path bucket_name")
  }

  private val localPath = new File(args(0))
  private val bucketName = args(1)

  if (!localPath.exists()) {
    throw new IllegalArgumentException("There is no such file or directory as %s".format(localPath))
  }

  new Sync(localPath, bucketName).sync

}

/**
 * Clones contents of a given directory to a bucket. It copies only files that have changed.
 *
 * @constructor create a new instance of Sync
 * @param localPath path to a folder on local FS
 * @param bucketName a name of your bucket
 *
 */

class Sync(localPath: File, bucketName: String) {

  private val client: AWSS3 = AWSS3(AWSCredentials())

  if (!client.exist(bucketName)) client.create(bucketName)

  private val bucket = client(bucketName)

  /**
   * synchronizes files
   *
   */
  def sync() {
    syncRecursively(localPath)

    def syncRecursively(dir: File) {
      for (file <- dir.listFiles()) {
        if (file.isDirectory()) syncRecursively(file)
        else {
          val key = file.getAbsolutePath().substring(localPath.getAbsolutePath().length())
          if (!bucket.exist(key) || (bucket(key).metadata.lastModified match {
            case None => true
            case Some(d) => d.before(new Date(file.lastModified()))
          })) {
            bucket(key) = file
          }
        }
      }
    }
  }

}