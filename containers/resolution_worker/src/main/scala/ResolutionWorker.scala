package org.image_processing.resolution_worker

import org.image_processing.common.dto.{FileName, fileNameRW}
import org.image_processing.common.transformer.BasicTransformer
import upickle.default
import com.sksamuel.scrimage
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter

class ResolutionWorker extends BasicTransformer {
    override val inputQueue: String = "scaling"
    override val outputQueue: String = "resizing"
    override val endEvent: String = "end"

    override type InputType = FileName
    override type OutputType = FileName

    override implicit val reader: default.Reader[InputType] = fileNameRW
    override implicit val writer: default.Writer[OutputType] = fileNameRW

    private val pngWriter: PngWriter = scrimage.nio.PngWriter.NoCompression

    override def transform(input: InputType): Option[OutputType] = {
        println(s"Scaling ${input.s}")
        val fileName = input.s
        val fileNameWithoutExtension = fileName.split('.').head
        val formattedFileName = s"${fileNameWithoutExtension}_scaled.png"

        try {
            val out = ImmutableImage.loader().fromFile(s"./shared/${fileName}").scaleTo(100, 100)
            out.output(pngWriter, s"./shared/${formattedFileName}")
            Some(FileName(formattedFileName))
        } catch {
            case e: java.io.IOException =>
                println(s"Error scaling ${input.s} - ${e.getMessage}")
                None
        }
    }
}
