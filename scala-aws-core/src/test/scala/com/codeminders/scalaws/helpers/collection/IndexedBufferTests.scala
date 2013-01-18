package com.codeminders.scalaws.helpers.collection

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.commons.math.util.MathUtils

@RunWith(classOf[JUnitRunner])
class IndexedBufferTests extends FunSuite {

  ignore("Fills IndexedBuffer with numbers and verify that their sum equals predefined number"){
    val ib = new IndexedBuffer[Int]()
    val N = 1024 * 19
    var c = 0
    while(c < N){
      ib(c) = c
      c += 1
    }
    assert(ib.foldLeft(0)((s, n) => s + n._2) === (0 until N).foldLeft(0)((s, n) => s + n))
  }
  
  ignore("Fills IndexedBuffer with strings"){
    val ib = new IndexedBuffer[String]()
    val N = 1024 * 19
    var c = 0
    while(c < N){
      ib(c) = "test"
      c += 1
    }
    ib.foreach(v => assert(v._2 === "test"))
  }
  
  ignore("Fills IndexedBuffer with strings and counts IndexedBuffer values"){
    val ib = new IndexedBuffer[String]()
    val N = 1024 * 19
    var c = 0
    while(c < N){
      ib(c) = "test"
      c += 1
    }
    assert(ib.size === N)
  }
  
  ignore("Fills IndexedBuffer with strings and converts it to array"){
    val ib = new IndexedBuffer[String]()
    val N = 1024 * 19
    var c = 0
    while(c < N){
      ib(c) = "test"
      c += 1
    }
    ib.toArray.foreach(v => assert(v._2 === "test"))
  }
  
}