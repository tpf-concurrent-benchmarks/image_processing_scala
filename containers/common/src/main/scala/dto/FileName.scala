package org.image_processing.common
package dto

import upickle.default

case class FileName(s: String)

val fileNameRW: default.ReadWriter[FileName] = default.readwriter[String].bimap[FileName](
  fileName => fileName.s,
  s => FileName(s)
)