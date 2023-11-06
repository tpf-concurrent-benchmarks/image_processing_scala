package org.image_processing.common
package stats

import com.timgroup.statsd.{NonBlockingStatsDClient, StatsDClient}
import config.MetricsConfig

object StatsDLogger extends MetricsLogger {
    var client: Option[StatsDClient] = None
    private val maxConnectionAttempts = 10
    private val sleepTimeBetweenConnectionAttempts = 5000

    def init(config: MetricsConfig): Unit = {
        println(s"Initializing StatsDLogger with prefix: ${config.prefix} in ${config.host}:${config.port}")
        client = retry(
            new NonBlockingStatsDClient(config.prefix, config.host, config.port),
            maxConnectionAttempts,
            sleepTimeBetweenConnectionAttempts,
            "Failed to connect to StatsD"
        )
        client match {
            case Some(_) => println("StatsDLogger initialized")
            case None => throw new Exception("Failed to connect to StatsD")
        }
    }

    private def withClient(f: StatsDClient => Unit): Unit = {
        client match {
            case Some(c) => f(c)
            case None => handleNotInitializedError()
        }
    }

    private def handleNotInitializedError(): Unit = {
        println("StatsLogger not initialized")
    }

    override def increment(metric: String): Unit = {
        withClient(_.increment(metric))
    }

    override def decrement(metric: String): Unit = {
        withClient(_.decrement(metric))
    }

    override def gauge(metric: String, value: Long): Unit = {
        withClient(_.gauge(metric, value))
    }

    override def runAndMeasure[T](metric: String, f: => T): T = {
        val startTime = System.currentTimeMillis()
        val result = f
        val endTime = System.currentTimeMillis()

        val duration = endTime - startTime
        withClient(_.recordExecutionTime(metric, duration.longValue()))

        result
    }
}