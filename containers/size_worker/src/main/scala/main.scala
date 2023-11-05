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
        StatsDLogger.init(MetricsConfig(config.getConfig("metrics")))
        config
    }
}

@main
def main(): Unit = {
    val config = getConfig
    val middlewareConfigData = config.getConfig("middleware")
    val resizingConfigData = config.getConfig("worker.size")
    val queuesConfig = QueuesConfig(middlewareConfigData)
    val resizingConfig = ResizingConfig(resizingConfigData)
    val rabbitMq = Rabbit(MiddlewareConfig(middlewareConfigData))
    SizeWorker(queuesConfig, resizingConfig).start(rabbitMq)
}