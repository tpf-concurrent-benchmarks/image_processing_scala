package org.image_processing.format_worker

import org.image_processing.common.config.QueuesConfig
import org.image_processing.common.dto.{FileName, fileNameRW}
import org.image_processing.common.image_utils.{ImageFormat, ImageUtils}
import org.image_processing.common.transformer.BasicTransformer
import upickle.default

object FormatWorker {
    def apply(queuesConfig: QueuesConfig): FormatWorker = {
        FormatWorker(
            queuesConfig.input,
            queuesConfig.output,
            queuesConfig.endEvent
        )
    }
}

case class FormatWorker(inputQueue: String,
                        outputQueue: String,
                        endEvent: String) extends BasicTransformer {
    override type InputType = FileName
    override type OutputType = FileName

    override implicit val reader: default.Reader[InputType] = fileNameRW
    override implicit val writer: default.Writer[OutputType] = fileNameRW

    override def transform(input: InputType): Option[OutputType] = {
        println(s"Formatting ${input.s}")
        val sourceFileName = input.s
        val sourceFileNameWithoutExtension = sourceFileName.split('.').head
        val targetFileName = s"${sourceFileNameWithoutExtension}_formatted.png"
        val sourceFilePath = s"./shared/$sourceFileName"
        val targetFilePath = s"./shared/$targetFileName"

        try {
            ImageUtils.format(sourceFilePath, targetFilePath, ImageFormat.Png())
            Some(FileName(targetFileName))
        } catch {
            case e: java.io.FileNotFoundException =>
                println(s"Input file $sourceFilePath not found - skipping")
                None
        }
    }
}
