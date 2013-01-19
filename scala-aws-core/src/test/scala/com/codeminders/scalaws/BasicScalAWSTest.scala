package com.codeminders.scalaws

import java.io.InputStream
import org.scalatest.FunSuite

trait BasicScalAWSTest extends FunSuite {

  def assertInputStreamsEqual(expected: InputStream, actual: InputStream) {
    val actualBuffer = Array.ofDim[Byte](1024 * 8)
    val expectedBuffer = Array.ofDim[Byte](1024 * 8)
    var actualRead = 0
    var expectedRead = 0
    var actualBufferPos = 0
    var expectedBufferPos = 0
    var totalBytesReadActual = 0
    var totalBytesReadExpected = 0
    do {
      actualRead = actual.read(actualBuffer, actualBufferPos, actualBuffer.size - actualBufferPos)
      expectedRead = expected.read(expectedBuffer, expectedBufferPos, expectedBuffer.size - expectedBufferPos)
      totalBytesReadActual += (if(actualRead > 0) actualRead else 0)
      totalBytesReadExpected += (if(expectedRead > 0) expectedRead else 0)
      actualBufferPos += actualRead
      expectedBufferPos += expectedRead
      if(actualRead > 0 || expectedRead > 0){
	      assert(actualBuffer.take(math.min(actualBufferPos, expectedBufferPos)).zip(expectedBuffer).forall(e => e._1 == e._2), "One of bytes from expected IS doesn't equal corresponding byte from actual IS")
	      if (actualBufferPos == expectedBufferPos) {
	    	 expectedBufferPos = 0
	    	 actualBufferPos = 0
	      } else if(actualBufferPos < expectedBufferPos){
	        System.arraycopy(expectedBuffer, actualBufferPos , expectedBuffer, 0, expectedBufferPos - actualBufferPos)
	        expectedBufferPos = expectedBufferPos - actualBufferPos
	        actualBufferPos = 0
	      }
	      else if(expectedBufferPos < actualBufferPos){
	        System.arraycopy(actualBuffer, expectedBufferPos , actualBuffer, 0, actualBufferPos - expectedBufferPos)
	        actualBufferPos = actualBufferPos - expectedBufferPos
	        expectedBufferPos = 0
	      }
      }
    } while (actualRead >= 0 || expectedRead >= 0)
    assert(totalBytesReadExpected === totalBytesReadActual, "There are more bytes read from one of the InputStreams")
  }

}