package org.image_processing.format_worker

import com.typesafe.config.ConfigFactory
import org.image_processing.common.config.FileConfigReader
import org.image_processing.common.middleware.Rabbit
import org.image_processing.common.stats.StatsDLogger
import upickle.default

def getConfigReader: FileConfigReader = {
  if (System.getenv("LOCAL") == "true") {
    println("-------------- Using local config --------------")
    FileConfigReader("format_worker_local.conf")
  } else {
    val config = ConfigFactory
        .parseString(s"metrics.prefix: ${System.getenv("NODE_ID")}")
        .withFallback(ConfigFactory.load("format_worker.conf"))
    val reader = FileConfigReader(config)
    StatsDLogger.init(reader.getMetricsConfig)
    reader

  }
}

@main
def main(): Unit = {
  val config = getConfigReader

  val rabbitMq = Rabbit(config.getMiddlewareConfig)
  new FormatWorker().start(rabbitMq)
}