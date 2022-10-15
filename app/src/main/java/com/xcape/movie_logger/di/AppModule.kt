package com.xcape.movie_logger.di

import android.app.Application
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.xcape.movie_logger.data.factory.ChartMediaTypeFactoryImpl
import com.xcape.movie_logger.data.local.database.CachedMediasDatabase
import com.xcape.movie_logger.data.local.database.UserDatabase
import com.xcape.movie_logger.data.local.type_converter.MediaConverters
import com.xcape.movie_logger.data.local.type_converter.UserConverters
import com.xcape.movie_logger.data.remote.IMDBApi
import com.xcape.movie_logger.data.repository.local.LocalUserRepositoryImpl
import com.xcape.movie_logger.data.repository.local.WatchedMediasRepositoryImpl
import com.xcape.movie_logger.data.repository.local.WatchlistRepositoryImpl
import com.xcape.movie_logger.data.repository.remote.AuthRepositoryImpl
import com.xcape.movie_logger.data.repository.remote.MovieRemoteRepositoryImpl
import com.xcape.movie_logger.data.repository.remote.RemoteUserRepositoryImpl
import com.xcape.movie_logger.domain.factory.ChartMediaTypeFactory
import com.xcape.movie_logger.domain.repository.local.WatchedMediasRepository
import com.xcape.movie_logger.domain.repository.local.WatchlistRepository
import com.xcape.movie_logger.domain.repository.remote.AuthRepository
import com.xcape.movie_logger.domain.repository.remote.MovieRemoteRepository
import com.xcape.movie_logger.domain.repository.local.LocalUserRepository
import com.xcape.movie_logger.domain.repository.remote.RemoteUserRepository
import com.xcape.movie_logger.domain.use_cases.Authenticator
import com.xcape.movie_logger.domain.use_cases.FirebaseAuthenticator
import com.xcape.movie_logger.domain.utils.Constants.MAIN_ENDPOINT
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
    // Api provider
    @Singleton
    @Provides
    fun provideIMDBApi(): IMDBApi {
        return Retrofit.Builder()
            .baseUrl(MAIN_ENDPOINT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IMDBApi::class.java)
    }

    // Remote repository provider
    @Singleton
    @Provides
    fun provideMovieRepository(api: IMDBApi): MovieRemoteRepository =
        MovieRemoteRepositoryImpl(api)

    // Watchlist medias database provider
    @Singleton
    @Provides
    fun provideCachedMediasDatabase(application: Application): CachedMediasDatabase {
        return Room.databaseBuilder(
            application,
            CachedMediasDatabase::class.java,
            CachedMediasDatabase.CACHED_MEDIAS_DATABASE
        )
        .fallbackToDestructiveMigration()
        .addTypeConverter(MediaConverters())
        .build()
    }

    // Watchlist medias repository provider
    @Singleton
    @Provides
    fun provideWatchlistRepository(database: CachedMediasDatabase, localUserRepository: LocalUserRepository): WatchlistRepository =
        WatchlistRepositoryImpl(database.watchlistDao, localUserRepository)

    // Watched medias repository provider
    @Singleton
    @Provides
    fun provideWatchedRepository(database: CachedMediasDatabase, localUserRepository: LocalUserRepository): WatchedMediasRepository
        = WatchedMediasRepositoryImpl(database.watchedListDao, localUserRepository)

    // Chart Media Type Factory
    @Singleton
    @Provides
    fun provideChartMediaTypeFactory(): ChartMediaTypeFactory
        = ChartMediaTypeFactoryImpl()

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth
        = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideAuthenticator(firebaseAuth: FirebaseAuth): Authenticator
        = FirebaseAuthenticator(firebaseAuth)

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore
        = Firebase.firestore

    @Singleton
    @Provides
    fun provideAuthRepository(
        auth: Authenticator,
        localUserRepository: LocalUserRepository,
        remoteUserRepository: RemoteUserRepository
    ): AuthRepository {
        return AuthRepositoryImpl(
            authenticator = auth,
            localUserRepository = localUserRepository,
            remoteUserRepository = remoteUserRepository
        )
    }

    @Singleton
    @Provides
    fun provideUserDatabase(application: Application): UserDatabase {
        return Room.databaseBuilder(
            application,
            UserDatabase::class.java,
            UserDatabase.USER_DATABASE
        )
            .fallbackToDestructiveMigration()
            .addTypeConverter(UserConverters())
            .build()
    }

    @Singleton
    @Provides
    fun provideLocalUserRepository(
        database: UserDatabase,
        watchlistRepository: WatchlistRepository,
        watchedMediasRepository: WatchedMediasRepository,
        remoteUserRepository: RemoteUserRepository
    ): LocalUserRepository =
        LocalUserRepositoryImpl(database.userDao, watchlistRepository, watchedMediasRepository, remoteUserRepository)

    @Singleton
    @Provides
    fun provideRemoteUserRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: Authenticator
    ): RemoteUserRepository =
        RemoteUserRepositoryImpl(firestore, firebaseAuth)

}