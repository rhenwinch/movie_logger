package com.xcape.movie_logger.di

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.xcape.movie_logger.data.remote.IMDBApi
import com.xcape.movie_logger.data.repository.MovieRepositoryImpl
import com.xcape.movie_logger.domain.repository.MovieRepository
import com.xcape.movie_logger.utils.Constants.MAIN_ENDPOINT
import com.xcape.movie_logger.utils.Constants.COLLECTION_PARENT
import com.xcape.movie_logger.utils.Constants.DOCUMENT_PARENT
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Api provider
    @Singleton
    @Provides
    fun provideIMDBApi(): IMDBApi =
        Retrofit.Builder()
            .baseUrl(MAIN_ENDPOINT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IMDBApi::class.java)

    // Repository provider
    @Singleton
    @Provides
    fun provideMovieRepository(api: IMDBApi): MovieRepository =
        MovieRepositoryImpl(api)
}