package org.image_processing.common
package config

import com.typesafe.config.Config

object MetricsConfig {
    def apply(config: Config): MetricsConfig = {
        val host = config.getString("host")
        val port = config.getInt("port")
        val prefix = config.getString("prefix")
        
        MetricsConfig(host, port, prefix)
    }
}

case class MetricsConfig(
                            host: String,
                            port: Int,
                            prefix: String
                        )
