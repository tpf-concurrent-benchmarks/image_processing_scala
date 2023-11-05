package org.image_processing.size_worker

import com.typesafe.config.{Config, ConfigFactory}
import org.image_processing.common.config.{MetricsConfig, MiddlewareConfig, QueuesConfig}
import org.image_processing.common.middleware.Rabbit
import org.image_processing.common.stats.StatsDLogger

def getConfig: Config = {
    if (System.getenv("LOCAL") == "true") {
        println("-------------- Using local config --------------")
        ConfigFactory.load("size_worker_local.conf")
    } else {
        val config = ConfigFactory
            .parseString(s"metrics.prefix: ${System.getenv("NODE_ID")}")
            .withFallback(ConfigFactory.load("size_worker.conf"))
        StatsDLogger.init(MetricsConfig(config.getConfig("middleware")))
        config
    }
}

@main
def main(): Unit = {
    val config = getConfig.getConfig("middleware")
    val rabbitMq = Rabbit(MiddlewareConfig(config))
    val queuesConfig = QueuesConfig(config)
    SizeWorker(queuesConfig).start(rabbitMq)
}