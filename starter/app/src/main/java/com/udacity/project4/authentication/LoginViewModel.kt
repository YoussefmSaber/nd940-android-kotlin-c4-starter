package com.udacity.project4.authentication

import androidx.lifecycle.*
import androidx.lifecycle.map
import com.google.firebase.auth.*

class LoginViewModel: ViewModel() {
    enum class Authentication {
        AUTHENTICATED, UNAUTHENTICATED
    }

    val authenticationState = FirebaseUserLiveData().map {
        if (it != null) {
            Authentication.AUTHENTICATED
        } else {
            Authentication.UNAUTHENTICATED
        }
    }
}

class FirebaseUserLiveData: LiveData<FirebaseUser?>() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val authState = FirebaseAuth.AuthStateListener {
        value = it.currentUser
    }

    override fun onActive() {
        firebaseAuth.addAuthStateListener(authState)
    }

    override fun onInactive() {
        firebaseAuth.removeAuthStateListener(authState)
    }
}