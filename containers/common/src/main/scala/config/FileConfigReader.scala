package org.image_processing.common
package config

import config.{MiddlewareConfig, QueuesConfig}

import com.typesafe.config.{Config, ConfigFactory}

object FileConfigReader {
    def apply(): FileConfigReader = {
        val config = ConfigFactory.load()
        FileConfigReader(config)
    }

    def apply(config: Config): FileConfigReader = {
        new FileConfigReader(config)
    }

    def apply(fileName: String): FileConfigReader = {
        val config = ConfigFactory.load(fileName)
        FileConfigReader(config)
    }
}

case class FileConfigReader(config: Config) extends ConfigReader {

    override def getMiddlewareConfig: MiddlewareConfig = {
        val middlewareHost = config.getString("middleware.host")
        val middlewarePort = config.getInt("middleware.port")
        val middlewareUser = config.getString("middleware.user")
        val middlewarePassword = config.getString("middleware.password")

        MiddlewareConfig(
            middlewareHost,
            middlewarePort,
            middlewareUser,
            middlewarePassword)
    }

    override def getQueuesConfig: QueuesConfig = {
        val workQueue = config.getString("middleware.queues.input")
        val resultsQueue = config.getString("middleware.queues.output")
        val endEvent = config.getString("middleware.endEvent")

        QueuesConfig(
            workQueue,
            resultsQueue,
            endEvent)
    }

    override def getMetricsConfig: MetricsConfig = {
        val metricsAddress = config.getString("metrics.host")
        val metricsPort = config.getInt("metrics.port")
        
        val metricsPrefix = if (config.hasPath("metrics.prefix")) {
            config.getString("metrics.prefix")
        } else {
            "image_processing"
        }

        MetricsConfig(
            metricsAddress,
            metricsPort,
            metricsPrefix)
    }

    override def getWorkConfig: WorkConfig = {
        val workPath = config.getString("data.path")

        WorkConfig(
            workPath)
    }
}
