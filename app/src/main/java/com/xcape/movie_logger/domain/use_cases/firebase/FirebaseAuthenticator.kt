package com.xcape.movie_logger.domain.use_cases.firebase

import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.xcape.movie_logger.common.Constants.APP_TAG
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface Authenticator {
    val loggedInUser: FirebaseUser?
    val currentUser: Flow<FirebaseUser?>
    suspend fun signUp(email: String, username: String, password: String): AuthResult?
    suspend fun signIn(email: String, password: String): AuthResult?
    suspend fun signOut()
}

class FirebaseAuthenticator @Inject constructor(
    private val authenticator: FirebaseAuth
) : Authenticator {
    override val loggedInUser: FirebaseUser?
        get() = authenticator.currentUser

    override val currentUser: Flow<FirebaseUser?>
        get() = callbackFlow {
            val authListener = AuthStateListener {
                trySend(it.currentUser)
            }

            authenticator.addAuthStateListener(authListener)
            awaitClose {
                authenticator.removeAuthStateListener(authListener)
                Log.d(APP_TAG, "Cancelling auth state listener!")
            }
        }

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