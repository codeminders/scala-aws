package com.codeminders.scalaws.helpers.io

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import java.io.ByteArrayInputStream
import scala.io.Source
import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.io.File
import java.io.FileOutputStream
import com.codeminders.scalaws.BasicScalAWSTest

@RunWith(classOf[JUnitRunner])
class SourceInputStreamTests extends BasicScalAWSTest {

  test("Verify SourceInputStream read methods") {
    val testData = "InputStream Data"
    val expectedBuffer = testData.getBytes()
    val is = new SourceInputStream(Source.fromString(testData))
    val buffer = Array.ofDim[Byte](expectedBuffer.length)
    // read second half of data
    is.read(buffer, testData.length() / 2, 1024)
    assert(buffer.drop(testData.length() / 2).zip(expectedBuffer).forall(e => e._1 == e._2))
    // read first half of data
    assert(expectedBuffer.drop(testData.length() / 2).forall(e => e == is.read()))
  }

  test("Verify SourceInputStream correctness") {
    assertInputStreamsEqual(
      getClass.getClassLoader().getResourceAsStream("50KText"),
      new SourceInputStream(Source.fromInputStream(getClass.getClassLoader().getResourceAsStream("50KText"))))
    assertInputStreamsEqual(
      getClass.getClassLoader().getResourceAsStream("UTF-8"),
      new SourceInputStream(Source.fromInputStream(getClass.getClassLoader().getResourceAsStream("UTF-8"))))
  }

}