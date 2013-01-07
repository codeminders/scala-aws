package com.codeminders.scalaws.s3

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import java.net.URL

@RunWith(classOf[JUnitRunner])
class RequestTests extends FunSuite {
  
  test("Verify 'apply' method"){
    assert(new com.codeminders.scalaws.http.Request(new URL("http://bucket.s3.amazonaws.com/")) === Request("bucket"))
    assert(new com.codeminders.scalaws.http.Request(new URL("http://s3.amazonaws.com/")) === Request())
    assert(new com.codeminders.scalaws.http.Request(new URL("http://bucket.s3.amazonaws.com/key")) === Request("bucket", "key"))
    assert(new com.codeminders.scalaws.http.Request(new URL("http://bucket.s3.amazonaws.com/key?p1=v1&p2=v2")) === Request("bucket", "key", Array(("p1", "v1"), ("p2", "v2"))))
  }

}