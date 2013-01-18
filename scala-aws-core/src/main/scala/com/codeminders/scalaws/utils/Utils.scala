package com.codeminders.scalaws.utils

import java.io.Closeable

object Utils {
  
  def using [T, C <: Closeable] (c : C)(block : C => T) : T = 
    try block(c)
    finally c.close

}