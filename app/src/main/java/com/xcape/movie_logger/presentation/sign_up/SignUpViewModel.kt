package com.xcape.movie_logger.presentation.sign_up

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.movie_logger.domain.repository.remote.AuthRepository
import com.xcape.movie_logger.domain.use_cases.EmailValidator
import com.xcape.movie_logger.domain.use_cases.PasswordValidator
import com.xcape.movie_logger.domain.use_cases.UsernameValidator
import com.xcape.movie_logger.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val emailValidator: EmailValidator,
    private val usernameValidator: UsernameValidator,
    private val passwordValidator: PasswordValidator
) : ViewModel() {

    val state: StateFlow<SignUpUIState>
    val accept: (SignUpUIAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<SignUpUIAction>()

        val isEmailTyping = actionStateFlow
            .filterIsInstance<SignUpUIAction.EmailTyping>()
            .distinctUntilChanged()
            .onStart { emit(SignUpUIAction.EmailTyping(isDoneTying = false, charactersTyped = "")) }

        val isUsernameTyping = actionStateFlow
            .filterIsInstance<SignUpUIAction.UsernameTyping>()
            .distinctUntilChanged()
            .onStart { emit(SignUpUIAction.UsernameTyping(isDoneTying = false, charactersTyped = "")) }

        val isPasswordTyping = actionStateFlow
            .filterIsInstance<SignUpUIAction.PasswordTyping>()
            .distinctUntilChanged()
            .onStart { emit(SignUpUIAction.PasswordTyping(isDoneTying = false, charactersTyped = "")) }

        val isConfirmPasswordTyping = actionStateFlow
            .filterIsInstance<SignUpUIAction.ConfirmPasswordTyping>()
            .distinctUntilChanged()
            .onStart { emit(SignUpUIAction.ConfirmPasswordTyping(isDoneTying = false, charactersTyped = "")) }

        val isSubmitting = actionStateFlow
            .filterIsInstance<SignUpUIAction.Submit>()
            .distinctUntilChanged()
            .onStart { emit(SignUpUIAction.Submit(isClicked = false)) }

        state = combine(
            combine(isEmailTyping, isUsernameTyping, ::Pair),
            combine(isPasswordTyping, isConfirmPasswordTyping, ::Pair),
            isSubmitting,
            ::Triple
        ).map { (textFields, passwordFields, formSubmit) ->
            val emailFlow = textFields.first
            val usernameFlow = textFields.second
            val passwordFlow = passwordFields.first
            val confirmPasswordFlow = passwordFields.second

            val email = emailFlow.charactersTyped.trim()
            val username = usernameFlow.charactersTyped.trim()
            val password = passwordFlow.charactersTyped.trim()
            val confirmPassword = confirmPasswordFlow.charactersTyped.trim()
            
            var state = SignUpUIState()

            // Validate forms before submitting
            val emailValidation = emailValidator.validate(email)
            val usernameValidation = usernameValidator.validate(username)
            val passwordValidation = passwordValidator.validate(password)
            val confirmPasswordValidation = passwordValidator.validateConfirmPassword(password, confirmPassword)

            var hasErrors = false
            if(!emailValidation.isSuccessful && emailFlow.isDoneTying) {
                hasErrors = true
                state = state.copy(emailError = emailValidation.error?.localizedMessage)
            }
            else if(!emailValidator.isEmailNotTaken(email).isSuccessful && emailFlow.isDoneTying) {
                hasErrors = true
                state = state.copy(emailError = "Email is already taken")
            }


            if(!usernameValidation.isSuccessful && usernameFlow.isDoneTying) {
                hasErrors = true
                state = state.copy(usernameError = usernameValidation.error?.localizedMessage)
            }
            else if(!usernameValidator.isUsernameNotTaken(username).isSuccessful && usernameFlow.isDoneTying) {
                hasErrors = true
                state = state.copy(usernameError = "Username is already taken")
            }

            if(!passwordValidation.isSuccessful && passwordFlow.isDoneTying) {
                hasErrors = true
                state = state.copy(passwordError = passwordValidation.error?.localizedMessage)
            }

            if(!confirmPasswordValidation.isSuccessful && confirmPasswordFlow.isDoneTying) {
                hasErrors = true
                state = state.copy(confirmPasswordError = confirmPasswordValidation.error?.localizedMessage)
            }

            if(formSubmit.isClicked && !hasErrors) {
                state = when(createAccount(email, username, password)) {
                    true -> state.copy(isAccountCreated = true)
                    false -> state.copy(isAccountCreated = false)
                }
            }

            return@map state
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SignUpUIState()
        )

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    private suspend fun createAccount(
        email: String,
        username: String,
        password: String,
    ): Boolean {
        return when(authRepository.signUp(email, username, password)) {
            is Resource.Success -> true
            is Resource.Error -> false
        }
    }
}