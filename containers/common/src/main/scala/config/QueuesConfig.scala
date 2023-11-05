package org.image_processing.common
package config

import com.typesafe.config.Config

object QueuesConfig {
    def apply(config: Config): QueuesConfig = {
        val input = config.getString("queues.input")
        val output = config.getString("queues.output")
        val endEvent = config.getString("endEvent")

        QueuesConfig(input, output, endEvent)
    }
}

case class QueuesConfig(
                           input: String,
                           output: String,
                           endEvent: String
                       )
