package com.codeminders.scalaws.s3.model

import java.io.InputStream

class S3Object(val bucket: Bucket, val key: Key, val content: InputStream, val contentLength: Long)