package org.image_processing.common
package stats

trait MetricsLogger {
    def increment(metric: String): Unit

    def decrement(metric: String): Unit

    def gauge(metric: String, value: Long): Unit

    def runAndMeasure[T](metric: String, f: => T): T
}

def getLogger: MetricsLogger = {
    if (System.getenv("LOCAL") == "true") {
        DummyLogger
    } else {
        StatsDLogger
    }
}
