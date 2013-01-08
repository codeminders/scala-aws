package com.codeminders.scalaws.helpers.io

import scala.io.Source
import java.io.InputStream
import org.apache.commons.io.IOUtils
import java.nio.charset.Charset
import java.nio.CharBuffer
import java.nio.ByteBuffer

class SourceInputStream(source: Source, encoding: String = "UTF-8") extends InputStream {

  val encoder = Charset.forName(encoding).newEncoder()

  override def read: Int = {
    val r = Array.ofDim[Byte](1)
    if (read(r, 0, 1) == -1) -1
    else r(0)
  }

  override def read(b: Array[Byte], off: Int, len: Int): Int = {
    val chars = source.take(math.min((b.size - off), len)).toArray
    if (chars.length < 1) -1
    else {
      val bb = ByteBuffer.wrap(b, off, b.size - off)
      var cr = encoder.encode(CharBuffer.wrap(chars, 0, chars.length), bb, true)
      if (!cr.isUnderflow())
        cr.throwException();
      cr = encoder.flush(bb);
      if (!cr.isUnderflow())
        cr.throwException();
      encoder.reset()
      bb.position()
    }
  }

}