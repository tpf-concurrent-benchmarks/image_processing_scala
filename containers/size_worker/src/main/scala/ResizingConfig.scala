package org.image_processing.size_worker

import com.typesafe.config.Config

object ResizingConfig {
    def apply(config: Config): ResizingConfig = {
        val targetWidth = config.getInt("targetWidth")
        val targetHeight = config.getInt("targetHeight")
        ResizingConfig(targetWidth, targetHeight)
    }
}

case class ResizingConfig(targetWidth: Int, targetHeight: Int)
