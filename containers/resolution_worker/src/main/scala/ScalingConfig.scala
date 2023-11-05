package org.image_processing.resolution_worker

import com.typesafe.config.Config

object ScalingConfig {
    def apply(config: Config): ScalingConfig = {
        val targetWidth = config.getInt("targetWidth")
        val targetHeight = config.getInt("targetHeight")
        ScalingConfig(targetWidth, targetHeight)
    }
}

case class ScalingConfig(targetWidth: Int, targetHeight: Int)
