package com.codeminders.scalaws.utils

object StringUtils {
  
  def removeCharacterDuplicates(str: String, c: Char): String = {
    if(str.isEmpty() || str.length() < 2) str
    str.zipWithIndex.foldLeft(new StringBuilder())((sb, e) => if (e._1 == c && e._2 > 0 && str(e._2 - 1) == c) sb else sb + e._1).toString
  }

}