package com.codeminders.scalaws.s3.examples

import com.codeminders.scalaws
import com.codeminders.scalaws.s3.AWSS3
import com.codeminders.scalaws.s3.model.Key
import com.codeminders.scalaws.s3.api.Keys
import com.codeminders.scalaws.s3.model.ObjectMetadata
import com.codeminders.scalaws.AWSCredentials
import com.codeminders.scalaws.s3.Implicits._

/**
 * KeysTree program entry point
 *
 * Usage:
 * <pre>
 * {@literal
 * KeysTree <i>bucket_name [delimiter]</i>
 * }
 * </pre>
 */

object KeysTree extends App {

  if (args.length > 2 || args.length < 1) {
    throw new IllegalArgumentException("Usage: " + KeysTree.getClass().getName() + " bucket [delimiter]")
  }

  private val bucketName = args(0)

  private val delimiter = if (args.length > 1) {
    args(1)
  } else "/"

  println("/")
  new KeysTree(bucketName, delimiter).printTree()

}

/**
 * Lists contents of a given bucket in a tree-like format.
 *
 *  e.g. :
 * <pre>
 * {@literal
 *   /
 *   |-1
 *   |-dir1/
 *   |     |-3
 *   |
 *   |-dir2/
 *   |     |-2
 *   |     |-4
 *   |     |-dir4/
 *   |     |     |-6
 *   |     |
 *   |     |-dir5/
 *   |           |-7
 *   |
 *   |-dir3/
 *         |-5
 * }
 * </pre>
 *
 * @constructor create a new instance of KeysTree
 * @param bucketName name of the bucket
 * @param delimiter delimiter, that is used to list objects in a bucket
 *
 */

class KeysTree(bucketName: String, delimiter: String = "/") {

  private val client: AWSS3 = AWSS3(AWSCredentials())

  /**
   * prints contents of a bucket
   *
   */
  def printTree(stream: Keys = client(bucketName).list(delimiter = delimiter), prefix: String = "") {

    stream.foreach {
      k =>
        println(prefix + "|-" + k.name.substring(stream.prefix.length()))
    }

    printPrefexesRecursively(stream.commonPrefexes)

    def printPrefexesRecursively(prefexes: Seq[Keys]) {
      prefexes match {
        case Seq(h) => {
          val prefixName = h.prefix.substring(stream.prefix.length())
          println(prefix + "|-" + prefixName)
          printTree(h, prefix + " " * (prefixName.length() + 1))
        }
        case Seq(h, t @ _*) => {
          val prefixName = h.prefix.substring(stream.prefix.length())
          println(prefix + "|-" + prefixName)
          printTree(h, prefix + "|" + " " * prefixName.length())
          printPrefexesRecursively(t)
        }
        case Seq() => println(prefix)
      }
    }

  }

}