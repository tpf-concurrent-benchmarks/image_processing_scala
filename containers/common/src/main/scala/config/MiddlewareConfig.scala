package org.image_processing.common
package config

import com.typesafe.config.Config

object MiddlewareConfig {
    def apply(config: Config): MiddlewareConfig = {
        MiddlewareConfig(
            config.getString("host"),
            config.getInt("port"),
            config.getString("user"),
            config.getString("password")
        )
    }
}

case class MiddlewareConfig(
                               host: String,
                               port: Int,
                               user: String,
                               password: String
                           )
