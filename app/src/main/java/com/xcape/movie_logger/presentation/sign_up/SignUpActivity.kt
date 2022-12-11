package com.xcape.movie_logger.presentation.sign_up

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.xcape.movie_logger.databinding.ActivitySignUpBinding
import com.xcape.movie_logger.common.Functions.px
import com.xcape.movie_logger.presentation.components.custom_extensions.activate
import com.xcape.movie_logger.presentation.components.custom_extensions.deactivate
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

const val IS_ACCOUNT_CREATED = "is_account_created"

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {
    private var _binding: ActivitySignUpBinding? = null
    private val binding: ActivitySignUpBinding
        get() = _binding!!

    // View models
    private val signUpViewModel: SignUpViewModel by viewModels()

    // Typing Jobs
    private val emailTypingJob: Job? = null
    private var usernameTypingJob: Job? = null
    private val passwordTypingJob: Job? = null
    private val confirmPasswordTypingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySignUpBinding.inflate(layoutInflater)

        setSupportActionBar(binding.signUpToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(binding.root)

        binding.bindState(
            uiState = signUpViewModel.state,
            uiAction = signUpViewModel.accept
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun ActivitySignUpBinding.bindState(
        uiState: StateFlow<SignUpUIState>,
        uiAction: (SignUpUIAction) -> Unit
    ) {
        // Bind Forms
        enumValues<AuthFormType>().forEach { type ->
            bindFormInputBoxes(
                formType = type,
                formChangeCallback = uiAction
            )
        }

        // Bind submit button
        signUpButton.setOnSingleClickListener {
            loadingBar.activate()
            signUpLabel.visibility = View.GONE

            submitForm(
                shouldSubmit = true,
                onSubmit = uiAction
            )
        }

        bindErrors(uiState = uiState)
        bindAccountCreationState(
            uiState = uiState,
            accountCreationCallback = uiAction
        )
    }

    private fun ActivitySignUpBinding.bindFormInputBoxes(
        formType: AuthFormType,
        formChangeCallback: (SignUpUIAction) -> Unit
    ) {
        val editText = getEditText(formType)
        var job = getAppropriateJob(formType)

        editText.addTextChangedListener {
            // Delay for 500 ms to check if user has stopped typing
            job?.cancel()
            job = lifecycleScope.launch {
                delay(500)

                if(!it.isNullOrBlank()) {
                    validateForm(
                        formType = formType,
                        onValidate = formChangeCallback
                    )
                }
                else {
                    val cardParent = editText.parent.parent.parent as MaterialCardView
                    val errorLabel = when(formType) {
                        AuthFormType.USERNAME -> usernameError
                        AuthFormType.EMAIL -> emailError
                        AuthFormType.PASSWORD -> passwordError
                        AuthFormType.CONFIRM_PASSWORD -> confirmPasswordError
                    }

                    cardParent.strokeWidth = 0
                    errorLabel.visibility = View.GONE
                }
            }
        }
    }

    private fun ActivitySignUpBinding.bindErrors(uiState: StateFlow<SignUpUIState>) {
        val isEmailValidationFailed = uiState
            .map { it.emailError }
            .distinctUntilChanged()

        val isUsernameValidationFailed = uiState
            .map { it.usernameError }
            .distinctUntilChanged()

        val isPasswordValidationFailed = uiState
            .map { it.passwordError }
            .distinctUntilChanged()

        val isConfirmPasswordValidationFailed = uiState
            .map { it.confirmPasswordError }
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

            // Username error
            launch {
                isUsernameValidationFailed.collect { errorMessage ->
                    val cardParent = (usernameEditText.parent.parent.parent as MaterialCardView)
                    usernameError.text = errorMessage

                    if(errorMessage.isNullOrEmpty()) {
                        cardParent.strokeWidth = 0
                        usernameError.visibility = View.GONE
                    }
                    else {
                        cardParent.strokeWidth = 1.px
                        usernameError.visibility = View.VISIBLE
                    }
                }
            }

            // Password error
            launch {
                isPasswordValidationFailed.collect { errorMessage ->
                    val cardParent = (passwordEditText.parent.parent.parent as MaterialCardView)
                    passwordError.text = errorMessage

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

            // Confirm password error
            launch {
                isConfirmPasswordValidationFailed.collect { errorMessage ->
                    val passwordCardParent = (passwordEditText.parent.parent.parent as MaterialCardView)
                    val confirmPasswordCardParent = (confirmPasswordEditText.parent.parent.parent as MaterialCardView)
                    confirmPasswordError.text = errorMessage

                    if(errorMessage.isNullOrEmpty()) {
                        passwordCardParent.strokeWidth = 0
                        confirmPasswordCardParent.strokeWidth = 0
                        confirmPasswordError.visibility = View.GONE
                    }
                    else {
                        passwordCardParent.strokeWidth = 1.px
                        confirmPasswordCardParent.strokeWidth = 1.px
                        confirmPasswordError.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun ActivitySignUpBinding.bindAccountCreationState(
        uiState: StateFlow<SignUpUIState>,
        accountCreationCallback: (SignUpUIAction) -> Unit
    ) {
        val isAccountCreated = uiState
            .map { it.isAccountCreated }
            .distinctUntilChanged()

        lifecycleScope.launch {
            isAccountCreated.collect { wasCreated ->
                if (wasCreated == false) {
                    // Stop progress bar
                    loadingBar.deactivate()
                    signUpLabel.visibility = View.VISIBLE

                    submitForm(
                        shouldSubmit = false,
                        onSubmit = accountCreationCallback
                    )
                    Snackbar.make(root, "Error on creating your account", Snackbar.LENGTH_SHORT)
                        .setAction("Retry") {
                            submitForm(true, onSubmit = accountCreationCallback)
                        }
                        .show()
                }
                else if (wasCreated == true) {
                    val intent = Intent()
                    intent.putExtra(IS_ACCOUNT_CREATED, true)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
    }

    private fun ActivitySignUpBinding.validateForm(
        formType: AuthFormType,
        onValidate: (SignUpUIAction) -> Unit
    ) {
        when(formType) {
            AuthFormType.EMAIL -> {
                val input = emailEditText.text.toString()
                onValidate(SignUpUIAction.EmailTyping(isDoneTying = true, charactersTyped = input))
            }
            AuthFormType.USERNAME -> {
                val input = usernameEditText.text.toString()
                onValidate(SignUpUIAction.UsernameTyping(isDoneTying = true, charactersTyped = input))
            }
            AuthFormType.PASSWORD -> {
                val input = passwordEditText.text.toString()
                onValidate(SignUpUIAction.PasswordTyping(isDoneTying = true, charactersTyped = input))
            }
            AuthFormType.CONFIRM_PASSWORD -> {
                val input = confirmPasswordEditText.text.toString()
                onValidate(SignUpUIAction.ConfirmPasswordTyping(isDoneTying = true, charactersTyped = input))
            }
        }
    }

    private fun submitForm(
        shouldSubmit: Boolean,
        onSubmit: (SignUpUIAction.Submit) -> Unit
    ) = onSubmit(SignUpUIAction.Submit(isClicked = shouldSubmit))

    private fun ActivitySignUpBinding.getEditText(formType: AuthFormType): TextInputEditText {
        return when(formType) {
            AuthFormType.USERNAME -> usernameEditText
            AuthFormType.EMAIL -> emailEditText
            AuthFormType.PASSWORD -> passwordEditText
            AuthFormType.CONFIRM_PASSWORD -> confirmPasswordEditText
        }
    }

    private fun getAppropriateJob(formType: AuthFormType): Job? {
        return when(formType) {
            AuthFormType.USERNAME -> usernameTypingJob
            AuthFormType.EMAIL -> emailTypingJob
            AuthFormType.PASSWORD -> passwordTypingJob
            AuthFormType.CONFIRM_PASSWORD -> confirmPasswordTypingJob
        }
    }
}