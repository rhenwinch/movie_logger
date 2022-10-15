package com.xcape.movie_logger.presentation.watched_list

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.ActivityWatchedMediasBinding
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.presentation.common.OnMediaClickListener
import com.xcape.movie_logger.presentation.components.custom_components.HorizontalMarginItemDecoration
import com.xcape.movie_logger.presentation.watched_list.adapters.WatchedMediasAdapter
import com.xcape.movie_logger.domain.utils.Functions.parseDate
import com.xcape.movie_logger.domain.utils.Functions.px
import com.xcape.movie_logger.domain.utils.Functions.toByteArray
import com.xcape.movie_logger.presentation.movie_details.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@AndroidEntryPoint
class WatchedMediasActivity : AppCompatActivity(), OnMediaClickListener {
    private var _binding: ActivityWatchedMediasBinding? = null
    private val binding: ActivityWatchedMediasBinding
        get() = _binding!!

    // View models
    private val watchedMediasViewModel: WatchedMediasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout for this fragment
        _binding = ActivityWatchedMediasBinding.inflate(layoutInflater)

        binding.bindViewPager(
            uiState = watchedMediasViewModel.state,
            data = watchedMediasViewModel.watchedMediaData,
            pageChangeCallback = watchedMediasViewModel.accept
        )

        setContentView(binding.root)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onMediaClick(
        mediaCategory: String,
        mediaId: String,
        mediaImageView: ImageView,
        mediaImageCard: MaterialCardView
    ) {
        val mediaImage = mediaImageView.drawable
            ?.toBitmap()
            ?.scale(146.px, 216.px, false)
            ?.toByteArray()

        // Proceed only if bitmap is not null
        mediaImage?.let { bitmap ->
            val intent = Intent(this, MovieActivity::class.java)
            intent.putExtra(MEDIA_ID, mediaId)
            intent.putExtra(MEDIA_CATEGORY, mediaCategory)
            intent.putExtra(MEDIA_BITMAP, bitmap)

            // Get media info
            val adapter = binding.watchedMediaViewPager.adapter as WatchedMediasAdapter
            val item = adapter.getItem( adapter.getItemPositionByProperty(mediaId) )
            intent.putExtra(MEDIA_ITEM, item?.mediaInfo)

            val options = ActivityOptions.makeSceneTransitionAnimation(
                this,
                mediaImageCard,
                "${mediaId}-${mediaCategory}"
            )

            // start the new activity
            startActivity(intent, options.toBundle())
        }
    }

    private fun ActivityWatchedMediasBinding.bindViewPager(
        uiState: StateFlow<WatchedMediasUIState>,
        data: Flow<List<WatchedMedia>>,
        pageChangeCallback: (WatchedMediasUIAction) -> Unit
    ) {
        val watchedMediasAdapter = WatchedMediasAdapter(listener = this@WatchedMediasActivity)
        watchedMediaViewPager.adapter = watchedMediasAdapter

        lifecycleScope.launch {
            data.collect { list ->
                if(list.isEmpty()) {
                    emptyListIndicator.visibility = View.VISIBLE
                    watchedListDataContainer.visibility = View.GONE
                }
                else {
                    emptyListIndicator.visibility = View.GONE
                    watchedListDataContainer.visibility = View.VISIBLE
                    watchedMediasAdapter.submitList(list)
                }
            }
        }

        // Setup ViewPager2 behavior
        // Implement dot indicators; and Page transformer
        // to preview next and previous pages in a view pager
        dotsIndicator.attachTo(watchedMediaViewPager)
        watchedMediaViewPager.apply {
            offscreenPageLimit = 1

            // https://stackoverflow.com/a/58088398/19371763
            val nextItemVisiblePx = resources.getDimension(R.dimen.viewpager_next_item_visible)
            val currentItemHorizontalMarginPx = resources.getDimension(R.dimen.viewpager_current_item_horizontal_margin)
            val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
            val transform = ViewPager2.PageTransformer { page, position ->
                val scaleFactor = 1 - (0.25F * abs(position))
                val alpha = 0 + (153  * abs(position)).toInt()

                page.translationX = -pageTranslationX * position
                page.foreground = ColorDrawable(Color.argb(alpha, 0, 0, 0))
                page.scaleY = scaleFactor
            }
            setPageTransformer(transform)
            registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val view = (get(0) as RecyclerView).layoutManager?.findViewByPosition(position)
                    updateCurrentPage(
                        uiState = uiState,
                        onPageChange = pageChangeCallback,
                        adapter = watchedMediasAdapter,
                        currentPage = view,
                        currentPagePosition = position
                    )
                }
            })

            addItemDecoration(HorizontalMarginItemDecoration(context, R.dimen.viewpager_current_item_horizontal_margin))
        }
    }
    
    private fun ActivityWatchedMediasBinding.updateCurrentPage(
        uiState: StateFlow<WatchedMediasUIState>,
        onPageChange: (WatchedMediasUIAction.Swipe) -> Unit,
        adapter: WatchedMediasAdapter,
        currentPage: View?,
        currentPagePosition: Int
    ) {
        onPageChange(WatchedMediasUIAction.Swipe(currentItemView = currentPage))
        val currentItemView = uiState
            .map { it.currentSwipedItem }
            .distinctUntilChanged()


        lifecycleScope.launch {
            currentItemView.collect { view ->
                val imageView = view?.findViewById<ImageView>(R.id.mediaImage)
                val bitmap = imageView?.drawable?.toBitmap()
                val itemOrNull = adapter.getItem(currentPagePosition)

                updateDynamicBackground(bitmap, itemOrNull?.mediaInfo?.gallery?.poster?.replace("_V1_", "_SL150_"))


                itemOrNull?.let { item ->
                    var mediaDuration: String? = item.mediaInfo?.duration
                    if (item.mediaInfo?.duration == "m")
                        mediaDuration = parseDate(item.mediaInfo.dateReleased)
                    else
                        mediaDuration += " | ${parseDate(item.mediaInfo?.dateReleased)}"

                    val simpleDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

                    val addedOn = item.addedOn?.let { date ->
                        simpleDateFormat.format(date)
                    }

                    watchedMediaMovieTitle.text = item.title
                    watchedMediaMovieDuration.text = mediaDuration
                    watchedMediaMovieRating.rating = item.rating.toFloat()
                    watchedMediaWatchedOnDate.text = addedOn
                }
            }
        }
    }

    private fun ActivityWatchedMediasBinding.updateDynamicBackground(
        bitmap: Bitmap?,
        imageUrl: String? = null
    ) {
        if (bitmap != null) {
            Palette.from(bitmap).generate { palette ->
                dynamicBackground.setBackgroundColor(palette?.vibrantSwatch?.rgb ?: ContextCompat.getColor(this@WatchedMediasActivity, R.color.transparent))
            }
        }
        else if(imageUrl != null) {
            Picasso.get()
                .load(imageUrl)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .into(object: Target {
                    override fun onBitmapLoaded(
                        bitmap: Bitmap?,
                        from: Picasso.LoadedFrom?
                    ) {
                        if (bitmap != null) {
                            Palette.from(bitmap).generate { palette ->
                                dynamicBackground.setBackgroundColor(palette?.vibrantSwatch?.rgb ?: ContextCompat.getColor(this@WatchedMediasActivity, R.color.transparent))
                            }
                        }
                    }

                    override fun onBitmapFailed(
                        e: Exception?,
                        errorDrawable: Drawable?
                    ) {}

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                })
        }
    }
}