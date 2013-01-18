package com.codeminders.scalaws

import java.io.InputStream
import org.scalatest.FunSuite

trait BasicScalAWSTest extends FunSuite {

  def assertInputStreamsEqual(expected: InputStream, actual: InputStream) {
    val actualBuffer = Array.ofDim[Byte](1024 * 8)
    val expectedBuffer = Array.ofDim[Byte](1024 * 8)
    var actualRead = 0
    var expectedRead = 0
    do {
      actualRead += actual.read(actualBuffer, actualRead, actualBuffer.size - actualRead)
      expectedRead += expected.read(expectedBuffer, expectedRead, expectedBuffer.size - expectedRead)
      if(actualRead > 0 || expectedRead > 0){
	      assert(actualBuffer.take(math.min(actualRead, expectedRead)).zip(expectedBuffer).forall(e => e._1 == e._2), "One of bytes from expected IS doesn't equal corresponding byte from actual IS")
	      if (actualRead == expectedRead) {
	    	 expectedRead = 0
	    	 actualRead = 0
	      } else if(actualRead < expectedRead){
	        System.arraycopy(expectedBuffer, actualRead , expectedBuffer, 0, expectedRead - actualRead)
	        expectedRead = expectedRead - actualRead
	        actualRead = 0
	      }
	      else if(expectedRead < actualRead){
	        System.arraycopy(actualBuffer, expectedRead , actualBuffer, 0, actualRead - expectedRead)
	        actualRead = actualRead - expectedRead
	        expectedRead = 0
	      }
      }
    } while (actualRead >= 0 && expectedRead >= 0)
    assert(actualRead < 0 && expectedRead < 0, "There are more bytes left in one of InputStreams")
  }

}