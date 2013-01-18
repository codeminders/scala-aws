package com.codeminders.scalaws.helpers.io

import java.io.InputStream

class EmptyInputStream extends InputStream {
  
  override def read: Int = -1
  
}

object EmptyInputStream{
  private val cachedEmptyInputStream = new EmptyInputStream()
  
  def apply(): EmptyInputStream = {
    cachedEmptyInputStream
  }
}