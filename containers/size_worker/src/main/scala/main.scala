package org.image_processing.size_worker

import com.typesafe.config.ConfigFactory
import org.image_processing.common.config.FileConfigReader
import org.image_processing.common.middleware.Rabbit
import org.image_processing.common.stats.StatsDLogger

def getConfigReader: FileConfigReader = {
  if (System.getenv("LOCAL") == "true") {
    println("-------------- Using local config --------------")
    FileConfigReader("size_worker_local.conf")
  } else {
    val config = ConfigFactory
        .parseString(s"metrics.prefix: ${System.getenv("NODE_ID")}")
        .withFallback(ConfigFactory.load("size_worker.conf"))
    val reader = FileConfigReader(config)
    StatsDLogger.init(reader.getMetricsConfig)
    reader

  }
}

@main
def main(): Unit = {
  val config = getConfigReader
  
  val rabbitMq = Rabbit(config.getMiddlewareConfig)
  new SizeWorker().start(rabbitMq)
}