package org.image_processing.common
package image_utils

import com.sksamuel.scrimage
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.format.{Format, FormatDetector}
import com.sksamuel.scrimage.nio.{GifWriter, ImageWriter, JpegWriter, PngWriter}

import java.io.File
import scala.annotation.tailrec

object ImageUtils {
    @throws[java.io.IOException]
    private def loadImage(path: String): ImmutableImage = ImmutableImage.loader().fromFile(path)

    @throws[java.io.IOException]
    private def loadImage(file: File): ImmutableImage = ImmutableImage.loader().fromFile(file)

    private def saveImage(image: ImmutableImage, path: String, writer: ImageWriter): Unit = image.output(writer, path)

    @tailrec
    private def getWriter(sourcePath: String, format: ImageFormat): ImageWriter = {
        format match {
            case ImageFormat.Png(compressionLevel: Int) => PngWriter(compressionLevel)
            case ImageFormat.Jpeg(quality: Int, progressive: Boolean) => JpegWriter(quality, progressive)
            case ImageFormat.Gif(progressive: Boolean) => GifWriter(progressive)
            case ImageFormat.Same =>
                val inputStream = new java.io.FileInputStream(sourcePath)
                val formatOpt = FormatDetector.detect(inputStream)
                inputStream.close()
                if (formatOpt.isEmpty) throw new Exception("Error detecting image format")
                val format = formatOpt.get
                format match {
                    case Format.PNG => getWriter(sourcePath, ImageFormat.Png())
                    case Format.JPEG => getWriter(sourcePath, ImageFormat.Jpeg())
                    case Format.GIF => getWriter(sourcePath, ImageFormat.Gif())
                    case _ => throw new Exception("Unsupported image format")
                }
        }
    }

    @throws[java.io.IOException]
    def resize(sourcePath: String, targetPath: String, width: Int, height: Int, format: ImageFormat = ImageFormat.Same): Unit = {
        val image = loadImage(sourcePath)
        val writer = getWriter(sourcePath, format)
        val resizedImage = image.scaleTo(width, height)
        saveImage(resizedImage, targetPath, writer)
    }

    @throws[java.io.IOException]
    def format(sourcePath: String, targetPath: String, format: ImageFormat): Unit = {
        val image = loadImage(sourcePath)
        val writer = getWriter(sourcePath, format)
        saveImage(image, targetPath, writer)
    }
}
