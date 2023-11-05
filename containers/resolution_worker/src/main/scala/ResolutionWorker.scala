package org.image_processing.resolution_worker

import com.sksamuel.scrimage
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import org.image_processing.common.config.QueuesConfig
import org.image_processing.common.dto.{FileName, fileNameRW}
import org.image_processing.common.transformer.BasicTransformer
import upickle.default

object ResolutionWorker {
    def apply(queuesConfig: QueuesConfig, scalingConfig: ScalingConfig): ResolutionWorker = {
        ResolutionWorker(
            queuesConfig.input,
            queuesConfig.output,
            queuesConfig.endEvent,
            scalingConfig.targetWidth,
            scalingConfig.targetHeight
        )
    }
}

case class ResolutionWorker(inputQueue: String,
                            outputQueue: String,
                            endEvent: String,
                            targetWidth: Int,
                            targetHeight: Int) extends BasicTransformer { 
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
            val out = ImmutableImage.loader().fromFile(s"./shared/$fileName").scaleTo(targetWidth, targetHeight)
            out.output(pngWriter, s"./shared/$formattedFileName")
            Some(FileName(formattedFileName))
        } catch {
            case e: java.io.IOException =>
                println(s"Error scaling ${input.s} - ${e.getMessage}")
                None
        }
    }
}
