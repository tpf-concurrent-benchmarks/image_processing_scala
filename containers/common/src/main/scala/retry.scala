package org.image_processing.common

import scala.annotation.tailrec

@tailrec
def retry[T](action: => T, attempts: Int, waitTimeBetweenAttempts: Int, failMsg: String = ""): Option[T] = {
    try {
        Some(action)
    } catch {
        case e: Exception =>
        if (attempts > 1) {
            println(s"$failMsg - $attempts attempt/s left")
            Thread.sleep(waitTimeBetweenAttempts)
            retry(action, attempts - 1, waitTimeBetweenAttempts, failMsg)
        } else {
            None
        }
    }
}


