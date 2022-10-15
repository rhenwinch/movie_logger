package com.xcape.movie_logger.data.repository.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.xcape.movie_logger.domain.model.user.FriendRequest
import com.xcape.movie_logger.domain.model.user.Notification
import com.xcape.movie_logger.domain.model.user.User
import com.xcape.movie_logger.domain.repository.remote.RemoteUserRepository
import com.xcape.movie_logger.domain.use_cases.Authenticator
import com.xcape.movie_logger.domain.utils.Constants.TAG
import com.xcape.movie_logger.domain.utils.Constants.USERS
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RemoteUserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: Authenticator
) : RemoteUserRepository {
    override suspend fun saveUser(
        userId: String,
        user: User,
        field: String?,
        value: Any?
    ) {
        try {
            if(!field.isNullOrEmpty()) {
                firestore.collection(USERS)
                    .document(userId)
                    .update(field, value)
                    .await()
            }
            else {
                firestore.collection(USERS)
                    .document(userId)
                    .set(user)
                    .await()
            }
        }
        catch (_: Exception) { }
    }

    override suspend fun getUser(userId: String): User? {
        return try {
            val uid = auth.currentUser?.uid ?: userId
            val result = firestore.collection(USERS)
                .document(uid)
                .get()
                .await()

            result.toObject()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getLatestFriends(): Flow<List<User>> {
        return callbackFlow {
            val friendsListListener = firestore.collection(USERS)
                .document(auth.currentUser!!.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        cancel(message = "Error fetching friends list", cause = e)
                        return@addSnapshotListener
                    }

                    val user = snapshot?.toObject(User::class.java)
                    user?.friends?.let { trySend(it) }
                }
            awaitClose {
                Log.d(TAG, "Cancelling friends list listener")
                friendsListListener.remove()
            }
        }
    }

    override fun getLatestNotifications(): Flow<List<Notification>> {
        return callbackFlow {
            val notificationsListener = firestore.collection(USERS)
                .document(auth.currentUser!!.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        cancel(message = "Error fetching notifications", cause = e)
                        return@addSnapshotListener
                    }

                    val user = snapshot?.toObject(User::class.java)
                    user?.notifications?.let { trySend(it) }
                }
            awaitClose {
                Log.d(TAG, "Cancelling notifications listener")
                notificationsListener.remove()
            }
        }
    }

    override fun getLatestFriendRequests(): Flow<List<FriendRequest>> {
        return callbackFlow {
            val friendRequestsListener = firestore.collection(USERS)
                .document(auth.currentUser!!.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        cancel(message = "Error fetching friend requests", cause = e)
                        return@addSnapshotListener
                    }

                    val user = snapshot?.toObject(User::class.java)
                    user?.friendRequests?.let { trySend(it) }
                }
            awaitClose {
                Log.d(TAG, "Cancelling friend requests listener")
                friendRequestsListener.remove()
            }
        }
    }

    override fun getLatestUnseenNotifications(): Flow<Int> {
        return callbackFlow {
            val unseenNotificationsListener = firestore.collection(USERS)
                .document(auth.currentUser!!.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        cancel(message = "Error fetching unseen notifications", cause = e)
                        return@addSnapshotListener
                    }

                    val user = snapshot?.toObject(User::class.java)
                    user?.unseenNotifications?.let { trySend(it) }
                }
            awaitClose {
                Log.d(TAG, "Cancelling unseen notifications listener")
                unseenNotificationsListener.remove()
            }
        }
    }

    override fun getLatestUnseenFriendRequests(): Flow<Int> {
        return callbackFlow {
            val unseenFriendRequestsListener = firestore.collection(USERS)
                .document(auth.currentUser!!.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        cancel(message = "Error fetching unseen friend requests", cause = e)
                        return@addSnapshotListener
                    }

                    val user = snapshot?.toObject(User::class.java)
                    user?.unseenFriendRequests?.let { trySend(it) }
                }
            awaitClose {
                Log.d(TAG, "Cancelling unseen friend requests listener")
                unseenFriendRequestsListener.remove()
            }
        }
    }
}