package org.image_processing.common
package middleware

import scala.concurrent.{Promise,Await,Future}

// Anonymous function that takes a message and returns a boolean
// indicating whether the message should be acked or not
type Callback = (Array[Byte]) => Boolean

trait MessageQueue {
    // @throws(classOf[IOException])
    def produce(queue: String, message: Array[Byte]): Unit

    // @throws(classOf[IOException])
    def setConsumer(queue: String, callback: Callback): Unit

    // @throws(classOf[IOException])
    def publish(eventName: String, message: Array[Byte]): Unit

    // @throws(classOf[IOException])
    def subscribe(eventName: String, callback: Callback): Unit

    // @throws(classOf[IOException], classOf[TimeoutException])
    def close(): Unit

    // @throws(classOf[IOException], classOf[TimeoutException])
    def startConsuming( until : Option[Future[Unit]] = None ): Unit = {
        try {
            val future = until match {
                case Some(f) => f
                case None => Promise[Unit]().future
            }
            Await.result(future, scala.concurrent.duration.Duration.Inf)
        } catch {
            case _: InterruptedException => println("Interrupted")
        }
    }
}
