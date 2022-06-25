package com.xcape.movie_logger.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.FragmentHomeBinding
import com.xcape.movie_logger.databinding.ItemDayBinding
import com.xcape.movie_logger.fragment.HomeFragment
import com.xcape.movie_logger.objects.day.Day
import com.xcape.movie_logger.ui.behavior.centerScrollToPosition
import java.time.format.TextStyle
import java.util.*

class HorizontalCalendarAdapter(
    private var daysOfMonthList: List<Day>,
    private val homeFragment: HomeFragment,
) : RecyclerView.Adapter<HorizontalCalendarAdapter.HorizontalCalendarViewHolder>() {
    private val context: Context = homeFragment.requireActivity().applicationContext
    private val binding: FragmentHomeBinding = homeFragment.binding

    override fun onBindViewHolder(holder: HorizontalCalendarViewHolder, position: Int) {
        daysOfMonthList[position].let { day ->
            holder.handleStyle(day)
            holder.bind(day)



            holder.itemView.setOnClickListener {
                onSelect(day)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HorizontalCalendarViewHolder {
        // Scroll to default selected day after creating the view
        binding.calendarRecyclerView.centerScrollToPosition(context, homeFragment.selectedDay.position)

        return HorizontalCalendarViewHolder.create(parent)
    }

    override fun getItemCount(): Int {
        return daysOfMonthList.size
    }

    private fun onSelect(day: Day) {
        daysOfMonthList.binarySearch {
            it.date.compareTo(day.date)
        }.let { currentPosition ->
            onSelect(currentPosition)
        }
    }

    private fun onSelect(position: Int) {
        daysOfMonthList.let {
            // Remove and edit the active style on the previous selected day
            val previousSelectedDayPosition = homeFragment.selectedDay.position
            daysOfMonthList[previousSelectedDayPosition].isSelected = false

            daysOfMonthList[position].isSelected = true // Select the new active/selected day

            notifyItemChanged(previousSelectedDayPosition)
            notifyItemChanged(position)

            homeFragment.selectedDay = daysOfMonthList[position]

            binding.calendarRecyclerView.centerScrollToPosition(context, position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateDaysOfMonthList(data: List<Day>) {
        daysOfMonthList = data
        notifyDataSetChanged()
    }

    class HorizontalCalendarViewHolder(private val binding: ItemDayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: Day) = binding.apply {
            dayWord.text = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ROOT)
            dayNumber.text = day.date.dayOfMonth.toString()
        }

        // Edit text appearances of each text of days and days of week
        fun handleStyle(day: Day) = binding.apply {
            val textAppearance = if (day.isSelected) {
                R.style.HorizontalCalendar_SelectedDayNumber to R.style.HorizontalCalendar_SelectedDayWord
            } else {
                R.style.HorizontalCalendar_DeselectedDayNumber to R.style.HorizontalCalendar_DeselectedDayWord
            }

            dayNumber.setTextAppearance(textAppearance.first)
            dayWord.setTextAppearance(textAppearance.second)
            selectedDateBackground.isVisible = day.isSelected
        }

        companion object {
            fun create(parent: ViewGroup): HorizontalCalendarViewHolder {
                val view = LayoutInflater.from(parent.context)
                val binding = ItemDayBinding.inflate(view, parent, false)
                return HorizontalCalendarViewHolder(binding)
            }
        }
    }
}