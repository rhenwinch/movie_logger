package com.xcape.movie_logger.fragment

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xcape.movie_logger.HomeListAdapter
import com.xcape.movie_logger.MovieApplication
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.FragmentHomeBinding
import com.xcape.movie_logger.fragment.dialog.DatePickerDialogFragment
import com.xcape.movie_logger.objects.day.Day
import com.xcape.movie_logger.objects.day.checkAfterDate
import com.xcape.movie_logger.ui.adapter.HorizontalCalendarAdapter
import com.xcape.movie_logger.ui.behavior.centerScrollToPosition
import com.xcape.movie_logger.ui.viewmodel.HomeFragmentViewModel
import com.xcape.movie_logger.ui.viewmodel.HomeFragmentViewModelFactory
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null // This property is only valid between onCreateView and onDestroyView.
    val binding get() = _binding!!

    var dateToday: LocalDate = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
    var selectedDay: Day = Day(true, dateToday, dateToday.dayOfMonth - 1)

    lateinit var calendarAdapter: HorizontalCalendarAdapter
    private val homeMovieListViewModel: HomeFragmentViewModel by viewModels {
        HomeFragmentViewModelFactory((activity?.application as MovieApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // Show the horizontal calendar
        showCalendar()

        // Get the list of movie items we have in the Dao
        getMovieListView()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getMovieListView() {
        val recyclerView = _binding?.homeMovieListView

        //searchMovie("Spiderman Far From Home") { possibleMovie ->
        //    try {
        //        possibleMovie[0]
        //    }
        //    catch (e: Error) {
        //        throw Error("Error accessing searched movies ${e.localizedMessage}")
        //    }
        //
        //    println("MOVIE ID: " + possibleMovie[0].id)
        //    getMovieInfo(possibleMovie[0].id) { movie ->
        //        movie.id = possibleMovie[0].id
        //        val listOfCasts = parsePeople(movie.actor)
        //        val listOfDirs = when(movie.director) {
        //            null -> parsePeople(movie.creator)
        //            else -> parsePeople(movie.director)
        //        }
        //
        //        val dataSource = (activity?.application as MovieApplication).database.movieDatabaseDao
        //
        //        val movieToAdd = Movie(
        //            dateWatched = "02-02-2022",
        //            movieTokenID = movie.id,
        //            movieTitle = movie.name,
        //            movieDesc = movie.description,
        //            movieGenre = movie.genre.joinToString(),
        //            movieDateReleased = parseDate(movie.datePublished),
        //            movieDuration = parseDuration(movie.duration),
        //            movieCast = listOfCasts,
        //            movieDirectors = listOfDirs,
        //            movieImagePoster = movie.image,
        //            movieImageThumb = movie.trailer.thumbnailUrl,
        //            movieRating = movie.aggregateRating.ratingValue
        //        )
        //
        //        dataSource.addMovie(movieToAdd)
        //
        //        println("ADDED THE MOVIE! => ${dataSource.getLatestMovie()!!.movieTitle}")
        //    }
        //}

        val adapter = HomeListAdapter()
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(activity?.applicationContext)

        homeMovieListViewModel.allMovies.observe(viewLifecycleOwner) { movie ->
            movie.let { adapter.submitList(it) }
        }

    }

    private fun showDatePickerDialog() {
        val fragmentManager = requireActivity().supportFragmentManager
        val datePickerView = DatePickerDialogFragment(this)
        datePickerView.show(fragmentManager, "Date Picker")
    }

    private fun showCalendar() {
        val calendarRecyclerView = binding.calendarRecyclerView

        // Insert the days of the given month into a list
        val daysOfMonthList = getDaysOfMonth()

        // Create the adapter
        calendarAdapter = createCalendarAdapter(daysOfMonthList, calendarRecyclerView)

        changeCalendarHeader()

        // Attach the dialog fragment
        attachDatePickerHandler()

        // Attach the handlers
        attachCalendarHandlers()
    }

    fun changeCalendarHeader() {
        // Edit the datePicker header
        val month = dateToday.month.toString().lowercase().replaceFirstChar {
            it.titlecase(Locale.getDefault())
        }
        val monthYearStr = "$month ${dateToday.year}"
        binding.datePickerHeader.text = monthYearStr
    }

    private fun createCalendarAdapter(
        daysOfMonthList: List<Day>,
        calendarRecyclerView: RecyclerView
    ): HorizontalCalendarAdapter {
        // Create adapter and initialize horizontal manager
        val adapter = HorizontalCalendarAdapter(daysOfMonthList, this)
        calendarRecyclerView.adapter = adapter
        calendarRecyclerView.layoutManager = LinearLayoutManager(activity?.applicationContext, LinearLayoutManager.HORIZONTAL, false)
        return adapter
    }

    private fun attachCalendarHandlers() {
        //val homeToolbar = binding.homeToolbarChild
        val previousMonth = binding.previousMonthButton
        val nextMonth = binding.nextMonthButton

        // Attach Listeners
        //scrollBackToCalendarDay(homeToolbar)
        previousMonth.setOnClickListener {
            goToMonth(-1)
        }
        nextMonth.setOnClickListener {
            goToMonth(1)
        }

    }

    private fun attachDatePickerHandler() {
        binding.apply {
            // Edit Calendar Button
            datePickerHeader.setOnClickListener {
                showDatePickerDialog()
            }
        }
    }

    private fun scrollBackToCalendarDay(homeToolbar: Toolbar) {
        homeToolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            if(item.itemId == R.id.calendarMenu) {
                val calendarRecyclerView = binding.calendarRecyclerView
                val nestedScrollView = binding.scrollParent
                nestedScrollView.fling(0)
                nestedScrollView.smoothScrollTo(0, 0)
                calendarRecyclerView.centerScrollToPosition(requireActivity().applicationContext, selectedDay.position)
            }

            return@OnMenuItemClickListener true
        })
    }

    private fun goToMonth(toAdd: Long) {
        dateToday = dateToday.plusMonths(toAdd)
        val daysOfPreviousMonthList = getDaysOfMonth()

        // Update the horizontal calendar view
        calendarAdapter.updateDaysOfMonthList(daysOfPreviousMonthList)
        changeCalendarHeader()
    }

    fun getDaysOfMonth(): List<Day> {
        val daysOfMonthList: MutableList<Day> = mutableListOf()
        var daysOfMonth = dateToday.withDayOfMonth(1)

        for (day in 0..31) {
            if(dateToday.checkAfterDate(daysOfMonth)) {
                daysOfMonthList.add(
                    Day(
                        (selectedDay.date.dayOfMonth == daysOfMonth.dayOfMonth), // If the selected day matches the iterated day
                        daysOfMonth,
                        day
                    )
                )
                daysOfMonth = daysOfMonth.plusDays(1)
            }
            else if(selectedDay.date.dayOfMonth == 31) {
                selectedDay = daysOfMonthList[29]
                daysOfMonthList[0].isSelected = true
                break
            }
            else break
        }

        return daysOfMonthList
    }
}