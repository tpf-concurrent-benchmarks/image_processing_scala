package org.image_processing.manager

import com.typesafe.config.{Config, ConfigFactory}
import org.image_processing.common.config.{MetricsConfig, MiddlewareConfig, QueuesConfig}
import org.image_processing.common.middleware.{MessageQueue, Rabbit}
import org.image_processing.common.stats.{StatsDLogger, getLogger}

import scala.concurrent.Promise

def getConfig: Config = {
  if (System.getenv("LOCAL") == "true") {
    println("-------------- Using local config --------------")
    ConfigFactory.load("manager_local.conf")
  } else {
    val config = ConfigFactory
        .parseString(s"metrics.prefix: ${System.getenv("NODE_ID")}")
        .withFallback(ConfigFactory.load("manager.conf"))
    StatsDLogger.init(MetricsConfig(config.getConfig("metrics")))
    config
  }
}

def getImagesBasename(imagesFolder: String): List[String] = {
    val imagesFolderFile = new java.io.File(imagesFolder)
    if (!imagesFolderFile.exists) {
        throw new Exception(s"Images folder $imagesFolder does not exist")
    }
    if (!imagesFolderFile.isDirectory) {
        throw new Exception(s"Images folder $imagesFolder is not a directory")
    }
    val images = imagesFolderFile.listFiles
    images.map(_.getName).toList
}

def sendWork(middleware: MessageQueue, imagesFolder: String): Int = {
    val imagesBasename = getImagesBasename(imagesFolder)
    imagesBasename.foreach { imageBasename =>
        val imageBasenameWithQuotes = "\"" + imageBasename + "\""
        middleware.produce("formatting", imageBasenameWithQuotes.getBytes("UTF-8"))
    }
    imagesBasename.size
}

def receiveResults(middleware: MessageQueue, resultsQueue: String, endEvent: String, resultsToWait: Int): Unit = {
    var resultsReceived = 0
    val allResultsReceived = Promise[Unit]()
    println(s"Waiting for $resultsToWait results")

    while (resultsReceived < resultsToWait) {
        middleware.setConsumer(resultsQueue, (message: Array[Byte]) => {
            val messageString = new String(message, "UTF-8")
            resultsReceived += 1
            if (resultsReceived == resultsToWait) {
                println(s"Got all results ($resultsReceived)")
                allResultsReceived.success(())
            }
            true
        })
    }
    middleware.startConsuming(Some(allResultsReceived.future))

    middleware.publish(endEvent, "end".getBytes("UTF-8"))
    middleware.close()
}

@main
def main(): Unit = {
    val config = getConfig.getConfig("middleware")
    val rabbitMq = Rabbit(MiddlewareConfig(config))
    val queuesConfig = QueuesConfig(config)

    val startTime = System.currentTimeMillis()
    val resultsToWait = sendWork(rabbitMq, "./shared/input")

    receiveResults(rabbitMq, queuesConfig.input, queuesConfig.endEvent, resultsToWait)
    val endTime = System.currentTimeMillis()
    println(s"Total time: ${endTime - startTime} ms")
    getLogger.gauge("completion_time", endTime - startTime)
}