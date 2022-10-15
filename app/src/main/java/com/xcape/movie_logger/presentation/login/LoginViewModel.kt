package com.xcape.movie_logger.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.movie_logger.domain.repository.remote.AuthRepository
import com.xcape.movie_logger.domain.use_cases.EmailValidator
import com.xcape.movie_logger.domain.use_cases.PasswordValidator
import com.xcape.movie_logger.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val emailValidator: EmailValidator,
    private val passwordValidator: PasswordValidator
) : ViewModel() {

    val state: StateFlow<LoginUIState>
    val accept: (LoginUIAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<LoginUIAction>()

        val isTypingOnEmail = actionStateFlow
            .filterIsInstance<LoginUIAction.EmailTyping>()
            .distinctUntilChanged()
            .onStart { emit(LoginUIAction.EmailTyping(isDoneTyping = false, charactersTyped = "")) }

        val isTypingOnPassword = actionStateFlow
            .filterIsInstance<LoginUIAction.PasswordTyping>()
            .distinctUntilChanged()
            .onStart { emit(LoginUIAction.PasswordTyping(isDoneTyping = false, charactersTyped = "")) }

        val isSubmitting = actionStateFlow
            .filterIsInstance<LoginUIAction.Submit>()
            .distinctUntilChanged()
            .onStart { emit(LoginUIAction.Submit(isClicked = false)) }

        state = combine(
            isTypingOnEmail,
            isTypingOnPassword,
            isSubmitting,
            ::Triple
        ).map { (emailTyping, passwordTyping, submit) ->
            val email = emailTyping.charactersTyped
            val password = passwordTyping.charactersTyped

            var state = LoginUIState()

            var hasInputError = false
            var loginError: String? = null
            val emailValidation = emailValidator.validate(email)
            val passwordValidation = passwordValidator.validate(password)

            if(emailTyping.isDoneTyping && !emailValidation.isSuccessful) {
                hasInputError = true
                state = state.copy(emailError = emailValidation.error?.localizedMessage)
            }
            else if(emailTyping.isDoneTyping && emailValidator.isEmailNotTaken(email).isSuccessful) {
                loginError = "This email is not registered yet"
            }

            if(passwordTyping.isDoneTyping && !passwordValidation.isSuccessful) {
                hasInputError = true
                state = state.copy(passwordError = passwordValidation.error?.localizedMessage)
            }

            if(submit.isClicked && !hasInputError) {
                val (isSuccessful, error) = signIn(email, password)
                state = when(isSuccessful) {
                    true -> state.copy(isUserLoggedIn = true)
                    false -> state.copy(isUserLoggedIn = false, loginError = loginError ?: error)
                }
            }

            return@map state
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoginUIState()
        )

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    private suspend fun signIn(email: String, password: String): Pair<Boolean, String?> {
        return when(val result = authRepository.signIn(email, password)) {
            is Resource.Success -> Pair(true, null)
            is Resource.Error -> Pair(false, result.message)
        }
    }
}