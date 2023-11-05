package org.image_processing.common
package config

import com.typesafe.config.Config

object QueuesConfig {
    def apply(config: Config): QueuesConfig = {
        val work = config.getString("queues.work")
        val results = config.getString("queues.results")
        val endEvent = config.getString("endEvent")

        QueuesConfig(work, results, endEvent)
    }
}

case class QueuesConfig(
                           input: String,
                           output: String,
                           endEvent: String
                       )
