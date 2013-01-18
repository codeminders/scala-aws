package com.codeminders.scalaws

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import scala.collection.mutable.ArrayBuffer

@RunWith(classOf[JUnitRunner])
class ConsistentMockInputStreamTests extends BasicScalAWSTest {
  
  test("Compose ByteArrayInputStream from ConsistentMockInputStream and compare them"){
    val bais = new ByteArrayInputStream((0 until (1024 * 1024 * 6)).foldLeft(ArrayBuffer[Byte]())((a, e) => a += e.toByte).toArray)
    assertInputStreamsEqual(bais, new ConsistentMockInputStream(0, 1024 * 1024 * 6))
  }

}