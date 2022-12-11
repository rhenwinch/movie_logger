package com.xcape.movie_logger.domain.use_cases.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.xcape.movie_logger.common.Constants
import com.xcape.movie_logger.data.repository.remote.REVIEWS
import com.xcape.movie_logger.data.repository.remote.WATCHLIST
import com.xcape.movie_logger.domain.model.user.WatchStatus
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


// Will check if post is liked or not
// Will check post's watch status
interface RawPostChecker {
    fun checkIfLiked(postLikes: List<String>): Boolean
    suspend fun checkWatchStatus(mediaId: String): WatchStatus
}

class FirebaseRawPostChecker @Inject constructor(
    private val auth: Authenticator,
    private val firestore: FirebaseFirestore
) : RawPostChecker {
    override fun checkIfLiked(postLikes: List<String>): Boolean {
        return postLikes.indexOf(auth.loggedInUser!!.uid) != -1
    }

    override suspend fun checkWatchStatus(mediaId: String): WatchStatus {
        val isInWatchlist = firestore.collection(Constants.USERS)
            .document(auth.loggedInUser!!.uid)
            .collection(WATCHLIST)
            .document(mediaId)
            .get()
            .await()

        val isRated = firestore.collection(Constants.USERS)
            .document(auth.loggedInUser!!.uid)
            .collection(REVIEWS)
            .document(mediaId)
            .get()
            .await()

        return when {
            isRated.exists() -> WatchStatus.WATCHED
            isInWatchlist.exists() -> WatchStatus.WATCHLIST
            else -> WatchStatus.NOT_WATCHED
        }
    }
}