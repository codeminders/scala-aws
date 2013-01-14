package com.codeminders.scalaws.helpers.io

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite

@RunWith(classOf[JUnitRunner])
class EmptyInputStreamTests extends FunSuite {
  
  test("Ensure that EmptyInputStreamTests is empty"){
    assert(new EmptyInputStream().available() === 0)
    assert(new EmptyInputStream().read === -1)
    assert(new EmptyInputStream().read(Array.ofDim[Byte](255), 0, 255) === -1)
  }

}