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
        val sourcePath = input.path
        val sourceName = input.name
        val sourceFileName = s"$sourcePath/$sourceName"

        val targetPath = "./shared/output"
        val targetFileName = s"$targetPath/$sourceName"

        try {
            ImageUtils.resize(sourceFileName, targetFileName, targetWidth, targetHeight, ImageFormat.Png())
            Some(FileName(targetPath, sourceName))
        } catch {
            case e: java.io.FileNotFoundException =>
                println(s"Input file $sourceFileName not found - skipping")
                None
        }
    }
}
