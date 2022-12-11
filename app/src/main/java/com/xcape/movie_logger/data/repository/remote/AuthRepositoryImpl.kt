package com.xcape.movie_logger.data.repository.remote

import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue.arrayRemove
import com.google.firebase.firestore.FieldValue.arrayUnion
import com.xcape.movie_logger.data.local.dao.FCMCredentialsDao
import com.xcape.movie_logger.domain.model.user.User
import com.xcape.movie_logger.domain.repository.remote.AuthRepository
import com.xcape.movie_logger.domain.repository.remote.UsersRepository
import com.xcape.movie_logger.domain.use_cases.firebase.Authenticator
import com.xcape.movie_logger.domain.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import java.lang.NullPointerException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authenticator: Authenticator,
    private val usersRepository: UsersRepository,
    private val fcmCredentialsDao: FCMCredentialsDao
) : AuthRepository {
    override fun getAuthUser(): FirebaseUser? {
        return authenticator.loggedInUser
    }

    override fun getCurrentAuthUser(): Flow<FirebaseUser?>
        = authenticator.currentUser

    override suspend fun signUp(
        email: String,
        username: String,
        password: String
    ): Resource<Boolean> {
        return try {
            val userId = authenticator.signUp(
                email = email,
                username = username,
                password = password
            )?.user?.uid ?: return Resource.Error(message = "Something went wrong")

            val user = User(
                userId = userId,
                username = username,
                fcmToken = listOf(fcmCredentialsDao.getToken().token)
                //lastUpdated = Calendar.getInstance().time
            )

            usersRepository.saveUser(userId = userId, user = user)
            Resource.Success(true)
        }
        catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(message = e.localizedMessage)
        }
    }

    override suspend fun signIn(email: String, password: String): Resource<Boolean> {
        return try {
            val uid = authenticator.signIn(email = email, password = password)?.user?.uid
                ?: return Resource.Error(data = false, message = "Something went wrong")

            val user = usersRepository.getUser(uid)!!
            // Add FCM Token of this App Device
            usersRepository.updateUserField(user.userId, "fcmToken", arrayUnion(fcmCredentialsDao.getToken().token))
            Resource.Success(data = true)
        }
        catch (e: FirebaseAuthInvalidCredentialsException) {
            Resource.Error(data = false, message = "Email or password is invalid")
        }
        catch (e: FirebaseTooManyRequestsException) {
            Resource.Error(data = false, message = "Too many requests, try again later")
        }
        catch (e: NullPointerException) {
            authenticator.signOut()
            Resource.Error(data = false, message = "Something went wrong")
        }
        catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(data = false, message = "Something went wrong")
        }
    }

    override suspend fun signOut(): Resource<Boolean> {
        return try {
            // Remove FCM Token of this device from DB
            usersRepository.updateUserField(authenticator.loggedInUser?.uid, "fcmToken", arrayRemove(fcmCredentialsDao.getToken().token))
            authenticator.signOut()
            Resource.Success(data = true)
        }
        catch (e: Exception) {
            Resource.Error(data = false, message = "ERR: ${e.localizedMessage}")
        }
    }
}