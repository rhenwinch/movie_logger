package com.xcape.movie_logger.data.repository.remote

import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.xcape.movie_logger.domain.model.user.User
import com.xcape.movie_logger.domain.repository.local.LocalUserRepository
import com.xcape.movie_logger.domain.repository.remote.AuthRepository
import com.xcape.movie_logger.domain.repository.remote.RemoteUserRepository
import com.xcape.movie_logger.domain.use_cases.Authenticator
import com.xcape.movie_logger.domain.utils.Resource
import java.lang.NullPointerException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authenticator: Authenticator,
    private val localUserRepository: LocalUserRepository,
    private val remoteUserRepository: RemoteUserRepository
) : AuthRepository {
    override fun isUserAlreadyAuthenticated(): Boolean
        = authenticator.currentUser != null

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
                username = username
                //lastUpdated = Calendar.getInstance().time
            )

            remoteUserRepository.saveUser(userId = userId, user = user)
            localUserRepository.saveUser(user = user)

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

            val user = remoteUserRepository.getUser(uid)!!
            localUserRepository.saveUser(user)
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
            authenticator.signOut()
            localUserRepository.removeUser()
            Resource.Success(data = true)
        }
        catch (e: Exception) {
            Resource.Error(data = false, message = "ERR: ${e.localizedMessage}")
        }
    }
}