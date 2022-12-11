package com.xcape.movie_logger.data.repository.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.xcape.movie_logger.domain.model.user.FriendRequest
import com.xcape.movie_logger.domain.model.user.User
import com.xcape.movie_logger.domain.repository.remote.FriendsRepository
import com.xcape.movie_logger.domain.repository.remote.UsersRepository
import com.xcape.movie_logger.domain.use_cases.firebase.Authenticator
import com.xcape.movie_logger.common.Constants
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

const val FRIEND_REQUESTS = "friendRequests"

class FriendsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: Authenticator,
    private val usersRepository: UsersRepository
) : FriendsRepository {
    override fun getLatestFriends(): Flow<List<User>> {
        return callbackFlow {
            val friendsListListener = firestore.collection(Constants.USERS)
                .whereArrayContains("friends", auth.loggedInUser!!.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        cancel(message = "Error fetching friends list", cause = e)
                        return@addSnapshotListener
                    }

                    if(snapshot?.documents != null && snapshot.documents.isNotEmpty()) {
                        trySend(snapshot.documents.map { it.toObject(User::class.java)!! })
                    }
                }
            awaitClose {
                Log.d(Constants.APP_TAG, "Cancelling friends list listener")
                friendsListListener.remove()
            }
        }
    }

    override fun getLatestFriendRequests(): Flow<List<FriendRequest>> {
        return callbackFlow {
            val friendRequestsListener = firestore.collection(Constants.USERS)
                .document(auth.loggedInUser!!.uid)
                .collection(FRIEND_REQUESTS)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        cancel(message = "Error fetching friend requests", cause = e)
                        return@addSnapshotListener
                    }
                    if(snapshot == null) {
                        cancel(message = "Error fetching friend requests")
                        return@addSnapshotListener
                    }

                    if(snapshot.documents.isNotEmpty()) {
                        val friendRequests = snapshot.documents.map { document ->
                            document.toObject(FriendRequest::class.java)!!
                        }
                        trySend(friendRequests)
                    }
                }
            awaitClose {
                Log.d(Constants.APP_TAG, "Cancelling friend requests listener")
                friendRequestsListener.remove()
            }
        }
    }

    override fun getLatestUnseenFriendRequests(): Flow<Int> {
        return callbackFlow {
            val unseenFriendRequestsListener = firestore.collection(Constants.USERS)
                .document(auth.loggedInUser!!.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        cancel(message = "Error fetching unseen friend requests", cause = e)
                        return@addSnapshotListener
                    }

                    val user = snapshot?.toObject(User::class.java)
                    user?.unseenFriendRequests?.let { trySend(it) }
                }
            awaitClose {
                Log.d(Constants.APP_TAG, "Cancelling unseen friend requests listener")
                unseenFriendRequestsListener.remove()
            }
        }
    }

    override fun updateFriends(friends: List<String>) {
        usersRepository.updateUserField(field = "friends", value = friends)
    }

    override fun updateFriendRequests(item: FriendRequest) {
        usersRepository.updateUserCollection(collection = "friendRequests", value = item)
    }

    override fun updateUnseenFriendRequests(friendRequests: Int) {
        usersRepository.updateUserField(field = "unseenFriendRequests", value = friendRequests)
    }
}