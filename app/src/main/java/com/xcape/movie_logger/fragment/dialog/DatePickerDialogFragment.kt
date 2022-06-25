package com.xcape.movie_logger.fragment.dialog

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.xcape.movie_logger.fragment.HomeFragment
import com.xcape.movie_logger.objects.day.Day
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


class DatePickerDialogFragment(
    private val homeFragment: HomeFragment
) : DialogFragment(), OnDateSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = localDateToCalendar(homeFragment.selectedDay.date)
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val dayOfMonth = calendar[Calendar.DAY_OF_MONTH]

        return DatePickerDialog(
            requireActivity(),
            this,
            year, month, dayOfMonth
        )
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = month
        calendar[Calendar.DAY_OF_MONTH] = dayOfMonth

        val newDate = calendarToLocalDate(calendar) // Set the new date
        homeFragment.dateToday = newDate
        val newDaysOfMonthList = homeFragment.getDaysOfMonth() // Get the new set of days of month date

        homeFragment.selectedDay = Day(true, newDate, newDate.dayOfMonth - 1)
        homeFragment.calendarAdapter.updateDaysOfMonthList(newDaysOfMonthList)
        homeFragment.changeCalendarHeader()
    }

    private fun localDateToCalendar(date: LocalDate): Calendar {
        val zonedDateTime = date.atStartOfDay(ZoneId.systemDefault())
        val instant = zonedDateTime.toInstant()
        val convertedDate = Date.from(instant)
        val calendar = Calendar.getInstance()
        calendar.time = convertedDate
        return calendar
    }

    private fun calendarToLocalDate(calendar: Calendar): LocalDate {
        val newInstant = calendar.toInstant()
        val newDateTimeZone = calendar.timeZone.toZoneId()
        val newDate = LocalDateTime.ofInstant(newInstant, newDateTimeZone)
        return newDate.toLocalDate()
    }
}