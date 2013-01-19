package com.codeminders.scalaws.s3.model

object BucketRegion extends Enumeration {
  type BucketRegion = Value
  val US_Standard = Value("")
  val US_West = Value("us-west-1")
  val US_West_2 = Value("us-west-2")
  val EU_Ireland = Value("EU")
  val AP_Singapore = Value("ap-southeast-1")
  val AP_Tokyo = Value("ap-northeast-1")
  val SA_SaoPaulo = Value("sa-east-1")
}
