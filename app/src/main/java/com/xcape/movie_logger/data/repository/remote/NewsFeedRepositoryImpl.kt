package com.xcape.movie_logger.data.repository.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.repository.remote.NewsFeedRepository
import com.xcape.movie_logger.domain.use_cases.firebase.Authenticator
import com.xcape.movie_logger.common.Constants
import com.xcape.movie_logger.common.Constants.APP_TAG
import com.xcape.movie_logger.domain.model.user.WatchStatus
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface NewsFeedPostCollector {
    fun onObtain(userId: String, list: List<WatchedMedia>)
}

class NewsFeedRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: Authenticator
) : NewsFeedRepository, NewsFeedPostCollector {
    private val posts = mutableMapOf<String, List<WatchedMedia>>()

    override fun getLatestPosts(): Flow<WatchedMedia> {
        return callbackFlow {
            val postsListener = firestore.collection(Constants.USERS)
                .whereArrayContains("friends", auth.loggedInUser!!.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        cancel(message = "Error fetching friends", cause = e)
                        return@addSnapshotListener
                    }

                    snapshot?.let {
                        it.documents.forEach { document ->
                            document
                                .reference
                                .collection(REVIEWS)
                                .addSnapshotListener { reviews, e ->
                                    if (e != null) {
                                        cancel(message = "Error fetching friends", cause = e)
                                        return@addSnapshotListener
                                    }

                                    reviews?.let { _reviews ->
                                        _reviews.documents.forEach { item ->
                                            trySend(item.toObject(WatchedMedia::class.java)!!)
                                        }
                                    }
                                }
                        }
                    }
                }

            awaitClose {
                Log.d(APP_TAG, "Cancelling posts listener")
                postsListener.remove()
            }
        }
    }

    override fun onObtain(userId: String, list: List<WatchedMedia>) {
        posts[userId] = list
    }
}