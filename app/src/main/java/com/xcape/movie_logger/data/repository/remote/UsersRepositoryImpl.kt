package com.xcape.movie_logger.data.repository.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.xcape.movie_logger.domain.model.user.User
import com.xcape.movie_logger.domain.repository.remote.UsersRepository
import com.xcape.movie_logger.domain.use_cases.firebase.Authenticator
import com.xcape.movie_logger.common.Constants
import com.xcape.movie_logger.common.Constants.APP_TAG
import com.xcape.movie_logger.common.Constants.USERS
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UsersRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: Authenticator
) : UsersRepository {
    override fun saveUser(userId: String, user: User) {
        try {
            val reference = firestore.collection(USERS)
                .document(userId)

            reference
                .set(user)
                .addOnSuccessListener { Log.d(APP_TAG, "User added!") }
                .addOnFailureListener { e -> Log.w(APP_TAG, "Error adding user", e) }

            // Create sub-collection of user
            reference
                .collection(FRIEND_REQUESTS)
            reference
                .collection(NOTIFICATIONS)
            reference
                .collection(WATCHLIST)
            reference
                .collection(REVIEWS)
        }
        catch (_: Exception) { }
    }

    override fun updateUserField(
        userId: String?,
        field: String,
        value: Any?
    ) {
        val uid = userId ?: auth.loggedInUser?.uid

        if (uid != null) {
            firestore.collection(USERS)
                .document(uid)
                .update(field, value)
                .addOnSuccessListener { Log.d(APP_TAG, "DocumentSnapshot successfully updated!") }
                .addOnFailureListener { e -> Log.w(APP_TAG, "Error updating document", e) }
        }
    }

    override fun updateUserCollection(userId: String?, collection: String, documentId: String?, value: Any) {
        val uid = userId ?: auth.loggedInUser?.uid ?: throw NullPointerException("User ID is empty!")

        val collectionRef = firestore.collection(USERS)
            .document(uid)
            .collection(collection)

        if(documentId == null) {
            collectionRef.add(value)
            return
        }

        collectionRef
            .document(documentId)
            .set(value)
    }

    override suspend fun getUser(userId: String?): User? {
        return try {
            val uid = userId ?: auth.loggedInUser?.uid
            val result = firestore.collection(USERS)
                .document(uid!!)
                .get()
                .await()

            result.toObject()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getLiveUser(userId: String?): Flow<User> {
        return callbackFlow {
            val uid = userId ?: auth.loggedInUser?.uid
            val userListener = firestore.collection(USERS)
                .document(uid!!)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        cancel(message = "Error fetching user", cause = e)
                        return@addSnapshotListener
                    }

                    val user = snapshot?.toObject(User::class.java)
                    user?.let { trySend(it) }
                }
            awaitClose {
                Log.d(Constants.APP_TAG, "Cancelling user listener")
                userListener.remove()
            }
        }
    }
}