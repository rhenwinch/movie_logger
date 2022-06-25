package com.xcape.movie_logger.objects.day

import java.time.DateTimeException
import java.time.LocalDate

data class Day(
    var isSelected: Boolean = false, // Determine if date is currently selected
    val date: LocalDate,
    var position: Int
)

fun LocalDate.checkAfterDate(tomorrowDate: LocalDate): Boolean {
    val monthLength = tomorrowDate.month.length(tomorrowDate.isLeapYear)

    try {
        this.withDayOfMonth(monthLength)
    } catch (e: DateTimeException) {
        return false
    }

    val lastDayOfMonth = this.withDayOfMonth(monthLength)
    return tomorrowDate.isBefore(lastDayOfMonth) or tomorrowDate.isEqual(lastDayOfMonth)
}