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
        val sourcePath = input.path
        val sourceName = input.name
        val sourceFileName = s"$sourcePath/$sourceName"
        println(s"Formatting $sourceFileName")

        val targetPath = "./shared/formatted"
        val sourceFileNameWithoutExtension = sourceName.split('.').head
        val targetName = s"$sourceFileNameWithoutExtension.png"
        val targetFileName = s"$targetPath/$targetName"

        try {
            ImageUtils.format(sourceFileName, targetFileName, ImageFormat.Png())
            Some(FileName(targetPath, targetName))
        } catch {
            case e: java.io.FileNotFoundException =>
                println(s"Input file $sourceFileName not found - skipping")
                None
        }
    }
}
