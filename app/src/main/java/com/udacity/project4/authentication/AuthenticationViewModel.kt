package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map


class AuthenticationViewModel : ViewModel() {

    enum class LoginMode {
        LOGGED, NOLOGGED
    }

    val authenticationMode = FirebaseUserLiveData().map { user ->
        if (user != null) {
            LoginMode.LOGGED
        } else {
            LoginMode.NOLOGGED
        }
    }

}