package org.image_processing.common
package config

import com.typesafe.config.Config

object MiddlewareConfig {
    def apply(config: Config): MiddlewareConfig = {
        val host = config.getString("host")
        val port = config.getInt("port")
        val user = config.getString("user")
        val password = config.getString("password")
        
        MiddlewareConfig(host, port, user, password)
    }
}

case class MiddlewareConfig(
                               host: String,
                               port: Int,
                               user: String,
                               password: String
                           )
