package com.xcape.movie_logger.data.repository.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.xcape.movie_logger.domain.model.user.Notification
import com.xcape.movie_logger.domain.model.user.User
import com.xcape.movie_logger.domain.repository.remote.NotificationsRepository
import com.xcape.movie_logger.domain.repository.remote.UsersRepository
import com.xcape.movie_logger.domain.use_cases.firebase.Authenticator
import com.xcape.movie_logger.common.Constants
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

const val NOTIFICATIONS = "notifications"

class NotificationsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: Authenticator,
    private val usersRepository: UsersRepository
) : NotificationsRepository {
    override fun getLatestNotifications(): Flow<List<Notification>> {
        return callbackFlow {
            val notificationsListener = firestore.collection(Constants.USERS)
                .document(auth.loggedInUser!!.uid)
                .collection(NOTIFICATIONS)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        cancel(message = "Error fetching notifications", cause = e)
                        return@addSnapshotListener
                    }
                    if(snapshot == null) {
                        cancel(message = "Error fetching notifications")
                        return@addSnapshotListener
                    }

                    if(snapshot.documents.isNotEmpty()) {
                        val notifications = snapshot.documents.map { document ->
                            document.toObject(Notification::class.java)!!
                        }
                        trySend(notifications)
                    }
                }

            awaitClose {
                Log.d(Constants.APP_TAG, "Cancelling notifications listener")
                notificationsListener.remove()
            }
        }
    }

    override fun getLatestUnseenNotifications(): Flow<Int> {
        return callbackFlow {
            val unseenNotificationsListener = firestore.collection(Constants.USERS)
                .document(auth.loggedInUser!!.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        cancel(message = "Error fetching unseen notifications", cause = e)
                        return@addSnapshotListener
                    }

                    val user = snapshot?.toObject(User::class.java)
                    user?.unseenNotifications?.let { trySend(it) }
                }
            awaitClose {
                Log.d(Constants.APP_TAG, "Cancelling unseen notifications listener")
                unseenNotificationsListener.remove()
            }
        }
    }

    override fun updateNotifications(to: String, item: Notification) {
        val docId = String.format("%s_%s", item.mediaId, item.from?.userId)
        usersRepository.updateUserCollection(userId = to, collection = "notifications", documentId = docId, value = item)
    }

    override fun updateUnseenNotifications(notifications: Int) {
        usersRepository.updateUserField(field = "unseenNotifications", value = notifications)
    }
}