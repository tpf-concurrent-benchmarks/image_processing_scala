package org.image_processing.resolution_worker

import org.image_processing.common.config.QueuesConfig
import org.image_processing.common.dto.{FileName, fileNameRW}
import org.image_processing.common.image_utils.{ImageFormat, ImageUtils}
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

    override def transform(input: InputType): Option[OutputType] = {
        val sourcePath = input.path
        val sourceName = input.name
        val sourceFileName = s"$sourcePath/$sourceName"
        println(s"Scaling $sourceFileName")

        val targetPath = "./shared/scaled"
        val targetFileName = s"$targetPath/$sourceName"

        try {
            ImageUtils.scale(sourceFileName, targetFileName, targetWidth, targetHeight, ImageFormat.Png())
            Some(FileName(targetPath, sourceName))
        } catch {
            case e: java.io.FileNotFoundException =>
                println(s"Input file $sourceFileName not found - skipping")
                None
        }
    }
}
