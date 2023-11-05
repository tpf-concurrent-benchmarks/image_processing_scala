package org.image_processing.common
package image_utils

enum ImageFormat {
    case Png(compressionLevel: Int = 0) // 0-9
    case Jpeg(quality: Int = 100, progressive: Boolean = false) // 0-100
    case Gif(progressive: Boolean = false)
    case Same
}
