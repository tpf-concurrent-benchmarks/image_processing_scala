package org.image_processing.size_worker

import org.image_processing.common.dto.{FileName, fileNameRW}
import org.image_processing.common.transformer.BasicTransformer
import upickle.default
import com.sksamuel.scrimage
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter

class SizeWorker extends BasicTransformer {
    override val inputQueue: String = "resizing"
    override val outputQueue: String = "results"
    override val endEvent: String = "end"

    override type InputType = FileName
    override type OutputType = FileName

    override implicit val reader: default.Reader[InputType] = fileNameRW
    override implicit val writer: default.Writer[OutputType] = fileNameRW

    private val pngWriter: PngWriter = scrimage.nio.PngWriter.NoCompression

    override def transform(input: InputType): Option[OutputType] = {
        println(s"Resizing ${input.s}")
        val fileName = input.s
        val fileNameWithoutExtension = fileName.split('.').head
        val formattedFileName = s"${fileNameWithoutExtension}_resized.png"

        try {
            val out = ImmutableImage.loader().fromFile(s"./shared/$fileName").resizeTo(30, 30)
            out.output(pngWriter, s"./shared/$formattedFileName")
            Some(FileName(input.s))
        } catch {
            case e: java.io.IOException =>
                println(s"Error resizing ${input.s} - ${e.getMessage}")
                None
        }
    }
}
