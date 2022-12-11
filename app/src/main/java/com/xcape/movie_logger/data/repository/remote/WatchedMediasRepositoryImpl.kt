package com.xcape.movie_logger.data.repository.remote

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.repository.remote.WatchedMediasRepository
import com.xcape.movie_logger.domain.use_cases.firebase.Authenticator
import com.xcape.movie_logger.common.Constants.APP_TAG
import com.xcape.movie_logger.common.Constants.USERS
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

const val REVIEWS = "reviews"

class WatchedMediasRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: Authenticator
) : WatchedMediasRepository {
    override fun getLatestWatchedMedias(): Flow<List<WatchedMedia>> {
        return callbackFlow {
            val reviewsListener = firestore.collection(USERS)
                .document(auth.loggedInUser!!.uid)
                .collection(REVIEWS)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        cancel(message = "Error fetching reviews", cause = e)
                        return@addSnapshotListener
                    }
                    if (snapshot == null) {
                        cancel(message = "Error fetching reviews")
                        return@addSnapshotListener
                    }

                    if (snapshot.documents.isNotEmpty()) {
                        val reviews = snapshot.documents.map { document ->
                            document.toObject(WatchedMedia::class.java)!!
                        }
                        trySend(reviews)
                    }
                }

            awaitClose {
                Log.d(APP_TAG, "Cancelling reviews listener")
                reviewsListener.remove()
            }
        }
    }

    override suspend fun getWatchedMediaByMediaId(mediaId: String, userId: String?): WatchedMedia? {
        val uid = userId ?: auth.loggedInUser!!.uid
        val result = firestore.collection(USERS)
            .document(uid)
            .collection(REVIEWS)
            .document(mediaId)
            .get()
            .await()

        return result.toObject(WatchedMedia::class.java)
    }

    override fun insertWatchedMedia(media: WatchedMedia) {
        updateReviews()
            .document(media.id)
            .set(media)
            .addOnSuccessListener {
                Log.d(APP_TAG, "DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e -> Log.w(APP_TAG, "Error writing document", e) }
    }

    override fun deleteWatchedMediaById(mediaId: String) {
        updateReviews()
            .document(mediaId)
            .delete()
            .addOnSuccessListener { Log.d(APP_TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(APP_TAG, "Error deleting document", e) }
    }

    override fun updateReviews(): CollectionReference {
        return firestore.collection(USERS)
            .document(auth.loggedInUser!!.uid)
            .collection(REVIEWS)
    }
}