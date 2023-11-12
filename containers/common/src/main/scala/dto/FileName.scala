package org.image_processing.common
package dto

import upickle.default

case class FileName(path: String, name: String)

val fileNameRW: default.ReadWriter[FileName] = default.readwriter[(String, String)].bimap[FileName](
  fileName => (fileName.path, fileName.name),
  tuple => FileName(tuple(0), tuple(1) )
)