package com.xcape.movie_logger.di

import android.app.Application
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.xcape.movie_logger.data.factory.ChartMediaTypeFactoryImpl
import com.xcape.movie_logger.data.remote.MediaApi
import com.xcape.movie_logger.data.repository.remote.*
import com.xcape.movie_logger.domain.factory.ChartMediaTypeFactory
import com.xcape.movie_logger.domain.repository.remote.FriendsRepository
import com.xcape.movie_logger.domain.repository.remote.NotificationsRepository
import com.xcape.movie_logger.domain.repository.remote.*
import com.xcape.movie_logger.common.Constants.MAIN_ENDPOINT
import com.xcape.movie_logger.data.local.dao.FCMCredentialsDao
import com.xcape.movie_logger.data.local.database.CredentialsDatabase
import com.xcape.movie_logger.data.remote.FCMApi
import com.xcape.movie_logger.data.utils.DispatchersProvider
import com.xcape.movie_logger.data.utils.DispatchersProviderImpl
import com.xcape.movie_logger.domain.use_cases.firebase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideMediaApi(): MediaApi {
        return Retrofit.Builder()
            .baseUrl(MAIN_ENDPOINT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MediaApi::class.java)
    }

    @Singleton
    @Provides
    fun provideFCMApi(): FCMApi {
        return Retrofit.Builder()
            .baseUrl(MAIN_ENDPOINT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FCMApi::class.java)
    }

    @Singleton
    @Provides
    fun provideCredentialsDatabase(application: Application): CredentialsDatabase {
        return Room.databaseBuilder(
            application,
            CredentialsDatabase::class.java,
            CredentialsDatabase.CREDENTIALS_DATABASE
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideFCMCredentialsDao(credentialsDb: CredentialsDatabase): FCMCredentialsDao {
        return credentialsDb.fcmCredentialsDao
    }

    @Singleton
    @Provides
    fun provideMediaRepository(api: MediaApi, credentialsDb: CredentialsDatabase): MediaRepository =
        MediaRepositoryImpl(api, credentialsDb.mediaApiCredentialsDao)

    @Singleton
    @Provides
    fun provideRemoteWatchlistRepository(
        firestore: FirebaseFirestore,
        auth: Authenticator
    ): WatchlistRepository =
        WatchlistRepositoryImpl(firestore, auth)

    @Singleton
    @Provides
    fun provideRemoteWatchedRepository(
        firestore: FirebaseFirestore,
        auth: Authenticator
    ): WatchedMediasRepository = WatchedMediasRepositoryImpl(firestore, auth)

    @Singleton
    @Provides
    fun provideChartMediaTypeFactory(): ChartMediaTypeFactory = ChartMediaTypeFactoryImpl()

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideAuthenticator(firebaseAuth: FirebaseAuth): Authenticator =
        FirebaseAuthenticator(firebaseAuth)

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore

    @Singleton
    @Provides
    fun provideAuthRepository(
        auth: Authenticator,
        usersRepository: UsersRepository,
        credentialsDb: CredentialsDatabase
    ): AuthRepository {
        return AuthRepositoryImpl(
            authenticator = auth,
            usersRepository = usersRepository,
            fcmCredentialsDao = credentialsDb.fcmCredentialsDao
        )
    }

    @Singleton
    @Provides
    fun provideRemoteUserRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: Authenticator
    ): UsersRepository = UsersRepositoryImpl(firestore, firebaseAuth)

    @Singleton
    @Provides
    fun provideRemoteNotificationsRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: Authenticator,
        usersRepository: UsersRepository
    ): NotificationsRepository =
        NotificationsRepositoryImpl(firestore, firebaseAuth, usersRepository)

    @Singleton
    @Provides
    fun provideRemoteFriendsRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: Authenticator,
        usersRepository: UsersRepository
    ): FriendsRepository = FriendsRepositoryImpl(firestore, firebaseAuth, usersRepository)

    @Singleton
    @Provides
    fun provideRemoteNewsFeedRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: Authenticator
    ): NewsFeedRepository = NewsFeedRepositoryImpl(firestore, firebaseAuth)

    @Singleton
    @Provides
    fun providePostLikerUseCase(
        firestore: FirebaseFirestore,
        firebaseAuth: Authenticator
    ): PostLiker = FirebasePostLikerUseCase(firebaseAuth, firestore)

    @Singleton
    @Provides
    fun provideRawPostCheckerUseCase(
        firestore: FirebaseFirestore,
        firebaseAuth: Authenticator
    ): RawPostChecker = FirebaseRawPostChecker(firebaseAuth, firestore)

    @Singleton
    @Provides
    fun provideNotificationSenderUseCase(
        api: FCMApi,
        notificationsRepository: NotificationsRepository
    ): NotificationSender = FirebaseNotificationSender(api, notificationsRepository)

    @Singleton
    @Provides
    fun provideDispatchers(): DispatchersProvider = DispatchersProviderImpl()
}
