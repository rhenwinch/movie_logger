package com.xcape.movie_logger.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xcape.movie_logger.domain.use_cases.EmailValidator
import com.xcape.movie_logger.domain.use_cases.PasswordValidator
import com.xcape.movie_logger.domain.use_cases.UsernameValidator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import javax.inject.Singleton


@Module
@InstallIn(ViewModelComponent::class)
class ValidatorModule {
    @Provides
    fun provideEmailValidator(firebaseAuth: FirebaseAuth): EmailValidator
        = EmailValidator(firebaseAuth = firebaseAuth)

    @Provides
    fun provideUsernameValidator(firestore: FirebaseFirestore): UsernameValidator
        = UsernameValidator(firestore = firestore)

    @Provides
    fun providePasswordValidator(): PasswordValidator
        = PasswordValidator()
}