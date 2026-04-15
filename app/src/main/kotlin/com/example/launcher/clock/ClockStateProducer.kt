package com.example.launcher.clock

import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ClockStateProducer {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    fun nowText(): String = LocalTime.now().format(formatter)
}
