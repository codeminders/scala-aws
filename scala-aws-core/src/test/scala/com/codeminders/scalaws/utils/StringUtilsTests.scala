package com.codeminders.scalaws.utils

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite

@RunWith(classOf[JUnitRunner])
class StringUtilsTests extends FunSuite {
  
  test("Verify 'removeCharacterDuplicates' method"){
    assert("/" === StringUtils.removeCharacterDuplicates("////", '/'))
    assert("" === StringUtils.removeCharacterDuplicates("", '/'))
    assert("aaaa" === StringUtils.removeCharacterDuplicates("aaaa", '/'))
    assert("/a/b/" === StringUtils.removeCharacterDuplicates("//a///b/", '/'))
    assert("/a" === StringUtils.removeCharacterDuplicates("/a", '/'))
  }

}