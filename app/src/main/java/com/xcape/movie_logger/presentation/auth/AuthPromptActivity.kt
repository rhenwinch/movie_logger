package com.xcape.movie_logger.presentation.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.xcape.movie_logger.databinding.ActivityAuthPromptBinding
import com.xcape.movie_logger.presentation.login.IS_LOGGED_IN
import com.xcape.movie_logger.presentation.login.IS_NOT_SIGNED_UP
import com.xcape.movie_logger.presentation.login.LoginActivity
import com.xcape.movie_logger.presentation.sign_up.IS_ACCOUNT_CREATED
import com.xcape.movie_logger.presentation.sign_up.SignUpActivity

class AuthPromptActivity : AppCompatActivity() {
    private var _binding: ActivityAuthPromptBinding? = null
    private val binding: ActivityAuthPromptBinding
        get() = _binding!!

    private var signUpActivityContract: ActivityResultLauncher<Intent>? = null
    private var loginActivityContract: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAuthPromptBinding.inflate(layoutInflater)
        binding.setupButtons()

        signUpActivityContract = setupActivityResultContract { result ->
            val isAccountCreated = result.data?.getBooleanExtra(
                IS_ACCOUNT_CREATED,
                false
            )
            if(result.resultCode == Activity.RESULT_OK && isAccountCreated == true) {
                finish()
            }
        }
        loginActivityContract = setupActivityResultContract { result ->
            val isUserLoggedIn = result.data?.getBooleanExtra(IS_LOGGED_IN, false)
            val isUserNotSignedUp = result.data?.getBooleanExtra(IS_NOT_SIGNED_UP, false)
            if(result.resultCode == Activity.RESULT_OK && isUserLoggedIn == true) {
                finish()
            }
            else if(result.resultCode == Activity.RESULT_CANCELED && isUserNotSignedUp == true) {
                launchActivityResultContract(signUpActivityContract, SignUpActivity::class.java)
            }
        }

        setContentView(binding.root)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun ActivityAuthPromptBinding.setupButtons() {
        notNowButton.setOnClickListener {
            onBackPressed()
        }

        loginButton.setOnClickListener {
            launchActivityResultContract(loginActivityContract, LoginActivity::class.java)
        }

        signUpButton.setOnClickListener {
            launchActivityResultContract(signUpActivityContract, SignUpActivity::class.java)
        }
    }

    private fun <T>launchActivityResultContract(
        activityContract: ActivityResultLauncher<Intent>?,
        activity: Class<T>
    ) {
        val intent = Intent(this@AuthPromptActivity, activity)
        activityContract?.launch(intent)
    }

    private fun setupActivityResultContract(shouldDo: (result: ActivityResult) -> Unit): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { shouldDo(it) }
    }
}