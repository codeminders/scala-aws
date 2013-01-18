package com.codeminders.scalaws.helpers.collection

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Buffer

//TODO: thread-safe
class IndexedBuffer[A <: Any](size: Int = 256) extends Traversable[(Int, A)] {

  private var buffer: Array[Any] = Array.ofDim(size)

  override def foreach[U](f: ((Int, A)) => U): Unit = {
    var c = 0
    for (e <- buffer) {
      if (e != null) {
        f((c, e.asInstanceOf[A]))
      }
      c += 1
    }
  }

  def update(idx: Int, elem: A): Unit = {
    if (buffer.size <= idx) {
      resize(math.min(idx * 2, Int.MaxValue))
    }
    buffer(idx) = elem
  }

  private def resize(newSize: Int) {
    val newBuffer: Array[Any] = Array.ofDim(newSize)
    buffer.copyToArray(newBuffer)
    buffer = newBuffer
  }

}