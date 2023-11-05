package org.image_processing.common
package config

import com.typesafe.config.Config

object MetricsConfig {
    def apply(config: Config): MetricsConfig = {
        MetricsConfig(
            host = config.getString("host"),
            port = config.getInt("port"),
            prefix = config.getString("prefix")
        )
    }
}

case class MetricsConfig(
                            host: String,
                            port: Int,
                            prefix: String
                        )
