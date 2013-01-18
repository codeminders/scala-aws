package com.codeminders.scalaws.s3.model

import java.util.Date

class MultipartUploadPart(val partNumber: Int, val lastModified: Date, val etag: String, val size: Int)