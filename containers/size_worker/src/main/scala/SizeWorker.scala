package org.image_processing.size_worker

import org.image_processing.common.dto.{FileName, fileNameRW}
import org.image_processing.common.transformer.BasicTransformer
import upickle.default
import org.image_processing.common.config.QueuesConfig
import org.image_processing.common.image_utils.{ImageFormat, ImageUtils}

object SizeWorker {
    def apply(queuesConfig: QueuesConfig, resizingConfig: ResizingConfig): SizeWorker = {
        SizeWorker(
            queuesConfig.input,
            queuesConfig.output,
            queuesConfig.endEvent,
            resizingConfig.targetWidth,
            resizingConfig.targetHeight
        )
    }
}

case class SizeWorker(inputQueue: String,
                      outputQueue: String,
                      endEvent: String,
                      targetWidth: Int,
                      targetHeight: Int) extends BasicTransformer {
    override type InputType = FileName
    override type OutputType = FileName

    override implicit val reader: default.Reader[InputType] = fileNameRW
    override implicit val writer: default.Writer[OutputType] = fileNameRW

    override def transform(input: InputType): Option[OutputType] = {
        println(s"Resizing ${input.s}")
        val sourceFileName = input.s
        val sourceFileNameWithoutExtension = sourceFileName.split('.').head
        val targetFileName = s"${sourceFileNameWithoutExtension}_resized.png"
        val sourceFilePath = s"./shared/$sourceFileName"
        val targetFilePath = s"./shared/$targetFileName"

        try {
            ImageUtils.resize(sourceFilePath, targetFilePath, targetWidth, targetHeight, ImageFormat.Png())
            Some(FileName(targetFileName))
        } catch {
            case e: java.io.FileNotFoundException =>
                println(s"Input file $sourceFilePath not found - skipping")
                None
        }
    }
}
