package com.example.cozypuzzle.core.common

import java.time.LocalDate

object DateUtils {

    fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

    fun dailyPuzzleDateKey(epochDay: Long = todayEpochDay()): String =
        LocalDate.ofEpochDay(epochDay).toString()

    fun isConsecutiveDay(previousEpochDay: Long, currentEpochDay: Long): Boolean =
        currentEpochDay - previousEpochDay == 1L
}
