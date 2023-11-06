package org.image_processing.resolution_worker

import org.image_processing.common.config.QueuesConfig
import org.image_processing.common.dto.FileName
import org.image_processing.common.stats.getLogger

object MeasuredResolutionWorker {
    def apply(queuesConfig: QueuesConfig, scalingConfig: ScalingConfig): MeasuredResolutionWorker = {
        new MeasuredResolutionWorker(
            queuesConfig.input,
            queuesConfig.output,
            queuesConfig.endEvent,
            scalingConfig.targetWidth,
            scalingConfig.targetHeight
        )
    }
}


class MeasuredResolutionWorker(inputQueue: String,
                               outputQueue: String,
                               endEvent: String,
                               targetWidth: Int,
                               targetHeight: Int) extends ResolutionWorker(inputQueue, outputQueue, endEvent, targetWidth, targetHeight) {
    override def transform(input: FileName): Option[FileName] = {
        val output = getLogger.runAndMeasure("work_time", super.transform(input))
        getLogger.increment("results_produced")
        output
    }
}
