package com.xcape.movie_logger.presentation.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.xcape.movie_logger.databinding.ActivityLoginBinding
import com.xcape.movie_logger.common.Functions.px
import com.xcape.movie_logger.presentation.components.custom_extensions.activate
import com.xcape.movie_logger.presentation.components.custom_extensions.deactivate
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener
import com.xcape.movie_logger.presentation.sign_up.AuthFormType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

const val IS_LOGGED_IN = "is_logged_in"
const val IS_NOT_SIGNED_UP = "is_not_signed_up"

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private var _binding: ActivityLoginBinding? = null
    private val binding: ActivityLoginBinding
        get() = _binding!!

    // View model
    private val loginViewModel: LoginViewModel by viewModels()

    // Typing job
    private var emailTypingJob: Job? = null
    private var passwordTypingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)

        setSupportActionBar(binding.loginToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.bindForm(
            uiState = loginViewModel.state,
            uiAction = loginViewModel.accept
        )

        setContentView(binding.root)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun ActivityLoginBinding.bindForm(
        uiState: StateFlow<LoginUIState>,
        uiAction: (LoginUIAction) -> Unit
    ) {
        bindErrors(uiState = uiState)

        bindUserLoginState(
            uiState = uiState,
            userLoginCallback = uiAction
        )

        loginButton.setOnSingleClickListener {
            toggleProgressBar(true)

            submitForm(
                shouldSubmit = true,
                onSubmit = uiAction
            )
        }

        emailEditText.addTextChangedListener {
            emailTypingJob?.cancel()
            emailTypingJob = lifecycleScope.launch {
                delay(500)

                if(!it.isNullOrBlank()) {
                    updateInputForm(
                        formType = AuthFormType.EMAIL,
                        onStoppedTyping = uiAction
                    )
                }
                else {
                    val cardParent = emailEditText.parent.parent.parent as MaterialCardView

                    cardParent.strokeWidth = 0
                    emailError.visibility = View.GONE
                }
            }
        }

        passwordEditText.addTextChangedListener {
            passwordTypingJob?.cancel()
            passwordTypingJob = lifecycleScope.launch {
                delay(500)

                if(!it.isNullOrBlank()) {
                    updateInputForm(
                        formType = AuthFormType.PASSWORD,
                        onStoppedTyping = uiAction
                    )
                }
                else {
                    val cardParent = passwordEditText.parent.parent.parent as MaterialCardView

                    cardParent.strokeWidth = 0
                    passwordError.visibility = View.GONE
                }
            }
        }
    }

    private fun ActivityLoginBinding.bindErrors(uiState: StateFlow<LoginUIState>) {
        val isEmailValidationFailed = uiState
            .map { it.emailError }
            .distinctUntilChanged()

        val isPasswordValidationFailed = uiState
            .map { it.passwordError }
            .distinctUntilChanged()

        lifecycleScope.launch {
            // Email error
            launch {
                isEmailValidationFailed.collect { errorMessage ->
                    emailError.text = errorMessage
                    val cardParent = (emailEditText.parent.parent.parent as MaterialCardView)

                    if(errorMessage.isNullOrEmpty()) {
                        cardParent.strokeWidth = 0
                        emailError.visibility = View.GONE
                    }
                    else {
                        cardParent.strokeWidth = 1.px
                        emailError.visibility = View.VISIBLE
                    }
                }
            }

            // Password error
            launch {
                isPasswordValidationFailed.collect { errorMessage ->
                    passwordError.text = errorMessage
                    val cardParent = (passwordEditText.parent.parent.parent as MaterialCardView)

                    if(errorMessage.isNullOrEmpty()) {
                        cardParent.strokeWidth = 0
                        passwordError.visibility = View.GONE
                    }
                    else {
                        cardParent.strokeWidth = 1.px
                        passwordError.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun ActivityLoginBinding.bindUserLoginState(
        uiState: StateFlow<LoginUIState>,
        userLoginCallback: (LoginUIAction) -> Unit
    ) {
        val isUserLoggedIn = uiState
            .map { it.isUserLoggedIn }
            .distinctUntilChanged()

        val isLoginFailed = uiState
            .map { it.loginError }
            .distinctUntilChanged()

        lifecycleScope.launch {
            combine(
                isUserLoggedIn,
                isLoginFailed,
                ::Pair
            ).collect { (loggedIn, loginError) ->
                if(!loggedIn && loginError != null) {
                    // Stop progress bar
                    toggleProgressBar(false)

                    submitForm(
                        shouldSubmit = false,
                        onSubmit = userLoginCallback
                    )

                    Snackbar.make(root, loginError, Snackbar.LENGTH_SHORT)
                        .setAction("OK") { }
                        .show()
                }
                else if(loggedIn) {
                    val intent = Intent()
                    intent.putExtra(IS_LOGGED_IN, true)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
    }

    private fun ActivityLoginBinding.toggleProgressBar(shouldShow: Boolean) {
        if(shouldShow) {
            loadingBar.activate()
            loginLabel.visibility = View.GONE
        }
        else {
            loadingBar.deactivate()
            loginLabel.visibility = View.VISIBLE
        }
    }

    private fun ActivityLoginBinding.updateInputForm(
        formType: AuthFormType,
        onStoppedTyping: (LoginUIAction) -> Unit
    ) {
        when(formType) {
            AuthFormType.EMAIL -> onStoppedTyping(LoginUIAction.EmailTyping(isDoneTyping = true, charactersTyped = emailEditText.text.toString()))
            AuthFormType.PASSWORD -> onStoppedTyping(LoginUIAction.PasswordTyping(isDoneTyping = true, charactersTyped = passwordEditText.text.toString()))
            else -> {}
        }
    }

    private fun submitForm(
        shouldSubmit: Boolean,
        onSubmit: (LoginUIAction.Submit) -> Unit
    ) = onSubmit(LoginUIAction.Submit(isClicked = shouldSubmit))
}