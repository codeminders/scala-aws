package com.codeminders.scalaws.s3.model

import java.util.Date
import StorageClass._

class Key(val name: String,
    val size: Option[Long] = None,
    val etag: Option[String] = None,
    val lastModified: Option[Date] = None,
    val storageClass: Option[StorageClass] = None, 
    val owner: Option[Owner] = None
    )