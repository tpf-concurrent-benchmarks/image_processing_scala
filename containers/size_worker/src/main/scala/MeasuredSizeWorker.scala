package org.image_processing.size_worker

import org.image_processing.common.config.QueuesConfig
import org.image_processing.common.dto.FileName
import org.image_processing.common.stats.getLogger

object MeasuredSizeWorker {
    def apply(queuesConfig: QueuesConfig, resizingConfig: ResizingConfig): MeasuredSizeWorker = {
        new MeasuredSizeWorker(
            queuesConfig.input,
            queuesConfig.output,
            queuesConfig.endEvent,
            resizingConfig.targetWidth,
            resizingConfig.targetHeight
        )
    }
}


class MeasuredSizeWorker(inputQueue: String,
                               outputQueue: String,
                               endEvent: String,
                               targetWidth: Int,
                               targetHeight: Int) extends SizeWorker(inputQueue, outputQueue, endEvent, targetWidth, targetHeight) {
    override def transform(input: FileName): Option[FileName] = {
        val output = getLogger.runAndMeasure("work_time", super.transform(input))
        getLogger.increment("results_produced")
        output
    }
}