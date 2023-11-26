package org.image_processing.common
package stats

object DummyLogger extends MetricsLogger {
    override def increment(metric: String): Unit = {
        println("Incrementing " + metric)
    }

    override def decrement(metric: String): Unit = {
        println("Decrementing " + metric)
    }

    override def gauge(metric: String, value: Long): Unit = {
        println("Gauging " + metric + " to " + value)
    }

    override def runAndMeasure[T](metric: String, f: => T): T = {
        val startTime = System.currentTimeMillis()
        val result = f
        val endTime = System.currentTimeMillis()

        val duration = endTime - startTime
        println("Running " + metric + " took " + duration + " ms")

        result
    }
}
