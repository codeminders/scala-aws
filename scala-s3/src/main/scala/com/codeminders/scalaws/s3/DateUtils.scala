package com.codeminders.scalaws.s3

import java.text.SimpleDateFormat
import java.util.SimpleTimeZone
import java.text.ParseException
import java.util.Date
import java.util.Locale

object DateUtils {

  private val iso8601DateFormat: SimpleDateFormat =
    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

  private val alternateIso8601DateFormat: SimpleDateFormat =
    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

  private val rfc822DateFormat: SimpleDateFormat =
    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

  iso8601DateFormat.setTimeZone(new SimpleTimeZone(0, "GMT"));
  rfc822DateFormat.setTimeZone(new SimpleTimeZone(0, "GMT"));
  alternateIso8601DateFormat.setTimeZone(new SimpleTimeZone(0, "GMT"));

  def parseIso8601Date(dateString: String): Date = {
    try {
      iso8601DateFormat.synchronized {
        return iso8601DateFormat.parse(dateString);
      }
    } catch {
      case e: ParseException => alternateIso8601DateFormat.synchronized {
        return alternateIso8601DateFormat.parse(dateString);
      }
    }
  }

  def formatIso8601Date(date: Date):String = {
    iso8601DateFormat.synchronized {
      return iso8601DateFormat.format(date);
    }
  }

  def parseRfc822Date(dateString: String):Date = {
    rfc822DateFormat.synchronized {
      return rfc822DateFormat.parse(dateString);
    }
  }

  def formatRfc822Date(date: Date):String = {
    rfc822DateFormat.synchronized {
      return rfc822DateFormat.format(date);
    }
  }

}