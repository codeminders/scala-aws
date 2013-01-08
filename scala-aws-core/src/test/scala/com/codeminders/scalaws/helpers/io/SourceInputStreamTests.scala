package com.codeminders.scalaws.helpers.io

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import java.io.ByteArrayInputStream
import scala.io.Source
import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.io.File

@RunWith(classOf[JUnitRunner])
class SourceInputStreamTests extends FunSuite {
  
  def nextFibonacciNumber(n: Int): Int = n match {

    case 0 | 1 => n
    case _ => nextFibonacciNumber(n - 1) + nextFibonacciNumber(n - 2)
  }
  
  def initializeByteArrayWithFibonacciNumbers(size: Int): Array[Byte] = {
    val arr = Array.ofDim[Byte](size)
    (0 to size).foreach{
      i => 
        arr(i) = nextFibonacciNumber(i).toByte
    }
    arr
  }

  test("Verify SourceInputStream for strings"){
    val s = IOUtils.toString(new SourceInputStream(Source.fromString("Данные")))
    assert("Данные" === s)
  }
  
  test("Verify SourceInputStream for files"){
    val expectedIS = getClass.getClassLoader().getResourceAsStream("50KText")
    val actualIS = new SourceInputStream(Source.fromInputStream(getClass.getClassLoader().getResourceAsStream("50KText")))
    assertInputStreamsEqual(expectedIS, actualIS)
  }
  
  test("Verify SourceInputStream read method"){
    val testData = "InputStream Data"
    val expectedBuffer = testData.getBytes() 
    val is = new SourceInputStream(Source.fromString(testData))
    val buffer = Array.ofDim[Byte](expectedBuffer.length)
    // read first half of data
    is.read(buffer, testData.length() / 2, 1024)
    assert(buffer.drop(testData.length() / 2).zip(expectedBuffer).forall(e => e._1 == e._2))
    // read second half of data
    is.read(buffer, 0, 1024)
    assert(buffer.zip(expectedBuffer.drop(testData.length() / 2)).forall(e => e._1 == e._2))
    
  }
  
  def assertInputStreamsEqual(expected: InputStream, actual:InputStream) {
    val actualBuffer = Array.ofDim[Byte](1024 * 8)
    val expectedBuffer = Array.ofDim[Byte](1024 * 8)
    var bytesRead = 0
    do{
      bytesRead = expected.read(expectedBuffer)
      assert(bytesRead === actual.read(actualBuffer))
      assert(actualBuffer.zip(expectedBuffer).forall(e => e._1 == e._2))
    }while(bytesRead > 0)
  }
  
}