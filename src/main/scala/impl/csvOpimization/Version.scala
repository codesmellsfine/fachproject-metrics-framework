package org.tud.sse.metrics
package impl

object Version extends Enumeration {
  type Version = Value
  val MAJORVERSION, MINORVERSION, HOTFIXVERSION, NOCHANGE = Value
}