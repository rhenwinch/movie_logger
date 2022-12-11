package com.xcape.movie_logger.data.repository.remote

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import com.xcape.movie_logger.domain.repository.remote.WatchlistRepository
import com.xcape.movie_logger.domain.use_cases.firebase.Authenticator
import com.xcape.movie_logger.common.Constants
import com.xcape.movie_logger.common.Constants.APP_TAG
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

const val WATCHLIST = "watchlist"

class WatchlistRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: Authenticator
) : WatchlistRepository {
    override fun getLatestWatchlistMedias(): Flow<List<WatchlistMedia>> {
        return callbackFlow {
            val watchlistListener = firestore.collection(Constants.USERS)
                .document(auth.loggedInUser!!.uid)
                .collection(WATCHLIST)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        cancel(message = "Error fetching watchlist", cause = e)
                        return@addSnapshotListener
                    }
                    if (snapshot == null) {
                        cancel(message = "Error fetching watchlist")
                        return@addSnapshotListener
                    }

                    if (snapshot.documents.isNotEmpty()) {
                        val watchlist = snapshot.documents.map { document ->
                            document.toObject(WatchlistMedia::class.java)!!
                        }
                        trySend(watchlist)
                    }
                }

            awaitClose {
                Log.d(Constants.APP_TAG, "Cancelling watchlist listener")
                watchlistListener.remove()
            }
        }
    }

    override suspend fun getWatchlistMediaByMediaId(mediaId: String): WatchlistMedia? {
        val result = firestore.collection(Constants.USERS)
            .document(auth.loggedInUser!!.uid)
            .collection(WATCHLIST)
            .document(mediaId)
            .get()
            .await()

        return result.toObject(WatchlistMedia::class.java)
    }

    override fun insertWatchlistMedia(media: WatchlistMedia) {
        updateWatchlist()
            .document(media.id)
            .set(media)
            .addOnSuccessListener { Log.d(APP_TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(APP_TAG, "Error writing document", e) }
    }

    override fun deleteWatchlistMediaById(mediaId: String) {
        updateWatchlist()
            .document(mediaId)
            .delete()
            .addOnSuccessListener { Log.d(APP_TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(APP_TAG, "Error deleting document", e) }
    }

    override fun deleteAll() {
        updateWatchlist()
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document
                        .reference
                        .delete()
                }
            }
            .addOnFailureListener { exception ->
                Log.w(APP_TAG, "Error getting documents: ", exception)
            }
    }

    override fun updateWatchlist(): CollectionReference {
        return firestore.collection(Constants.USERS)
            .document(auth.loggedInUser!!.uid)
            .collection(WATCHLIST)
    }
}