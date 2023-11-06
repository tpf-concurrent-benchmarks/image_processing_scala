package org.image_processing.format_worker

import org.image_processing.common.config.QueuesConfig
import org.image_processing.common.dto.FileName
import org.image_processing.common.stats.getLogger

object MeasuredFormatWorker {
    def apply(queuesConfig: QueuesConfig): MeasuredFormatWorker = {
        new MeasuredFormatWorker(
            queuesConfig.input,
            queuesConfig.output,
            queuesConfig.endEvent
        )
    }
}


class MeasuredFormatWorker(inputQueue: String,
                           outputQueue: String,
                           endEvent: String) extends FormatWorker(inputQueue, outputQueue, endEvent) {
    override def transform(input: FileName): Option[FileName] = {
        val output = getLogger.runAndMeasure("work_time", super.transform(input))
        getLogger.increment("results_produced")
        output
    }
}
