package com.example.cozypuzzle.core.common

import android.view.View
import java.util.concurrent.TimeUnit

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun Long.formatDuration(): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(this)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "%d:%02d".format(minutes, seconds) else "${seconds}s"
}
