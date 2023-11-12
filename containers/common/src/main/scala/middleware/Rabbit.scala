package org.image_processing.common
package middleware

import com.newmotion.akka.rabbitmq
import com.rabbitmq.client.{Channel, Connection}
import config.MiddlewareConfig

object Rabbit {
    private val maxConnectionAttempts = 20
    private val waitTimeBetweenConnectionAttempts = 5000

    def apply(host: String, port: Int, user: String, password: String, prefetchCount: Int): Rabbit = {
        val factory: rabbitmq.ConnectionFactory = new rabbitmq.ConnectionFactory()
        
        factory.setHost(host)
        factory.setPort(port)
        factory.setUsername(user)
        factory.setPassword(password)

        val connectionOpt = retry(
            factory.newConnection(),
            maxConnectionAttempts,
            waitTimeBetweenConnectionAttempts,
            "Failed to connect to RabbitMQ")
        connectionOpt match {
            case Some(connection) => new Rabbit(connection, prefetchCount)
            case None => throw new Exception("Failed to connect to RabbitMQ")
        }
    }

    def apply(config: MiddlewareConfig, prefetchCount: Int): Rabbit = {
        Rabbit.apply(config.host, config.port, config.user, config.password, prefetchCount)
    }

    def apply(config: MiddlewareConfig): Rabbit = {
        Rabbit.apply(config, 10)
    }
}

class Rabbit(connection: rabbitmq.Connection, prefetchCount: Int) extends MessageQueue {
    private val channel: rabbitmq.Channel = connection.createChannel()
    channel.basicQos(prefetchCount)
    channel.confirmSelect()

    override def produce(queue: String, message: Array[Byte]): Unit = {
        _produce(queue, message)
    }

    private def _produce(queue: String, message: Array[Byte], resendWaitTimeMS:Long = 1000, retries:Int=5): Unit = {
        (1 to retries).takeWhile(attempt => {
            channel.basicPublish("", queue, null, message)

            if (channel.waitForConfirms()) {
                false // success
            } else {
                Thread.sleep(resendWaitTimeMS)
                true // retry
            }
        })   
    }

    def produceOrDie(queue: String, message: Array[Byte], resendWaitTimeMS:Long = 1000, retries:Int=5): Unit = {
        Range.inclusive(1, retries).takeWhile(attempt => {
            channel.basicPublish("", queue, null, message)

            if (channel.waitForConfirms()) {
                false // success
            } else {
                if (attempt == retries) throw new Exception("Failed to publish message to RabbitMQ")
                Thread.sleep(resendWaitTimeMS)
                true // retry
            }
        })   
    }

    override def setConsumer(queue: String, callback: Callback): Unit = {
        val consumer = new rabbitmq.DefaultConsumer(channel) {
            override def handleDelivery(
                                           consumerTag: String,
                                           envelope: rabbitmq.Envelope,
                                           properties: rabbitmq.BasicProperties,
                                           body: Array[Byte]
                                       ): Unit = {
                if (callback(body)) {
                    channel.basicAck(envelope.getDeliveryTag, false)
                } else {
                    channel.basicNack(envelope.getDeliveryTag, false, true)
                }
            }
        }
        channel.basicConsume(queue, false, consumer)
    }

    override def publish(eventName: String, message: Array[Byte]): Unit = {
        channel.basicPublish(eventName, "", null, message)
    }

    override def subscribe(eventName: String, callback: Callback): Unit = {
        val queueName = channel.queueDeclare().getQueue

        channel.queueBind(queueName, eventName, "")

        setConsumer(queueName, callback)
    }

    override def close(): Unit = {
        channel.close()
        connection.close()
    }
}
