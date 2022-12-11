package com.xcape.movie_logger.domain.use_cases.firebase

import com.google.firebase.firestore.FieldValue.*
import com.google.firebase.firestore.FirebaseFirestore
import com.xcape.movie_logger.data.repository.remote.REVIEWS
import com.xcape.movie_logger.common.Constants.USERS
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface PostLiker {
    suspend fun onLike(ownerId: String, postId: String)
    suspend fun onDislike(ownerId: String, postId: String)
}

class FirebasePostLikerUseCase @Inject constructor(
    private val auth: Authenticator,
    private val firestore: FirebaseFirestore
) : PostLiker {
    override suspend fun onLike(ownerId: String, postId: String) {
        firestore.collection(USERS)
            .document(ownerId)
            .collection(REVIEWS)
            .document(postId)
            .update("likes", arrayUnion(auth.loggedInUser!!.uid))

        if(auth.loggedInUser!!.uid != ownerId)
            firestore.collection(USERS)
                .document(ownerId)
                .update("unseenNotifications", increment(1))
    }

    override suspend fun onDislike(ownerId: String, postId: String) {
        firestore.collection(USERS)
            .document(ownerId)
            .collection(REVIEWS)
            .document(postId)
            .update("likes", arrayRemove(auth.loggedInUser!!.uid))

        firestore.collection(USERS)
            .document(ownerId)
            .collection(REVIEWS)
            .document("${postId}_${ownerId}")
            .delete()
    }
}