package com.codeminders.scalaws

import java.io.InputStream

class ConsistentMockInputStream(off: Int = 0, size: Int = Int.MaxValue) extends InputStream {

  private var currentValue = off

  override def read: Int = {
    if(currentValue >= size ){
      -1
    } 
    else {
	    val b = currentValue.toByte
	    currentValue += 1
	    b
    }
  }
  
  override def read(b: Array[Byte], off: Int, len: Int): Int = {
    if(currentValue >= size ){
      -1
    }
    else {
	    val l = math.min(math.min(b.size - off, size - currentValue), len)
	    (currentValue until (currentValue + l)).zipWithIndex.foreach(v => b(off + v._2) = v._1.toByte)
	    currentValue += l
	    l
    }
  }

}