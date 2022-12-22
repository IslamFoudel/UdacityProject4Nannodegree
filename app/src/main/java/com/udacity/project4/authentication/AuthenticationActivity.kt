package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_authentication.*
import java.util.Observer

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticationBinding
    private val viewModel: AuthenticationViewModel by viewModels()

    companion object {
        const val LOG_IN_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // implementing binding by uncommented binding in gradle(starter code),
        // and then get root via this lines:
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // observe the mode for login mode, if it is not loggin yet, launch the flow of sing in to login,
        // with gmail auth or email and pass
        viewModel.authenticationMode.observe(this, Observer { loginStatus ->
            if (loginStatus == AuthenticationViewModel.LoginMode.NOLOGGED) {
                binding.btnLogin.setOnClickListener {
                    launchSignInFlow()
                }
            }
            // this line if the user is loggin before, so go derectly to ReminderActivity.
            else if (loginStatus == AuthenticationViewModel.LoginMode.LOGGED) {
                val intent = Intent(this, RemindersActivity::class.java)
                startActivity(intent)
            }
        })
    }

    private fun launchSignInFlow() {
        // let users to choose between sign in or register with email or Google account,
        // If users choose to register with email,
        // they will going to create a password.
        val providers =
            arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
            )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            LOG_IN_CODE
        )
    }
}
