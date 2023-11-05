package org.image_processing.common
package transformer

import middleware.MessageQueue

import upickle.default

import scala.concurrent.Promise
import upickle.default.{Reader, Writer, read, write}

trait BasicTransformer {
    val inputQueue: String
    val outputQueue: String
    val endEvent: String
    type InputType
    type OutputType
    implicit val reader: Reader[InputType]
    implicit val writer: Writer[OutputType]

    def transform(input: InputType): Option[OutputType]

    def start(middleware: MessageQueue): Unit = {
        middleware.setConsumer(inputQueue, input => {
            val convertedInput = read[InputType](input)

            val output = transform(convertedInput)
            output.foreach(o => {
                val outputString = default.write(o)
                middleware.produce(outputQueue, outputString.getBytes("UTF-8"))
            })
            true
        })
        val stopPromise = Promise[Unit]()

        middleware.subscribe(endEvent, end => {
            println(s"Received end: ${new String(end, "UTF-8")}")
            stopPromise.success(())
            true
        })
        println("Starting to consume")
        middleware.startConsuming(Some(stopPromise.future))
        middleware.close()
    }
}
