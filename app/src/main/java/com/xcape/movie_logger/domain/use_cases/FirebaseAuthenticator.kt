package com.xcape.movie_logger.domain.use_cases

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.xcape.movie_logger.domain.utils.Resource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface Authenticator {
    val currentUser: FirebaseUser?
    suspend fun signUp(email: String, username: String, password: String): AuthResult?
    suspend fun signIn(email: String, password: String): AuthResult?
    suspend fun signOut()
}

class FirebaseAuthenticator @Inject constructor(
    private val authenticator: FirebaseAuth
) : Authenticator {
    override val currentUser: FirebaseUser?
        get() = authenticator.currentUser

    override suspend fun signUp(email: String, username: String, password: String): AuthResult? {
        val task = authenticator.createUserWithEmailAndPassword(email, password).await()
        val authenticatedUser = task.user ?: return null
        authenticatedUser.updateProfile(userProfileChangeRequest { displayName = username })
        return task
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        return authenticator.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun signOut() {
        authenticator.signOut()
    }
}