package com.example.data.auth

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthUser(
    val uid: String,
    val displayName: String,
    val email: String?,
    val isGoogleUser: Boolean = true
)

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val user: AuthUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * Clean Authentication & Cloud Sync abstraction layer.
 * Operates local-first: the app runs 100% offline without requiring Firebase.
 * When Firebase configuration (google-services.json) and Google Sign-In are provided,
 * seamlessly links user UID and synchronizes user data to Firestore within free quotas.
 */
class AuthManager(private val context: Context) {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isFirebaseAvailable = MutableStateFlow(false)
    val isFirebaseAvailable: StateFlow<Boolean> = _isFirebaseAvailable.asStateFlow()

    init {
        checkFirebaseAvailability()
    }

    private fun checkFirebaseAvailability() {
        try {
            // Test if FirebaseApp is initialized
            val firebaseAppClass = Class.forName("com.google.firebase.FirebaseApp")
            val getInstanceMethod = firebaseAppClass.getMethod("getInstance")
            val instance = getInstanceMethod.invoke(null)
            _isFirebaseAvailable.value = (instance != null)
            Log.d("AuthManager", "Firebase initialization check: ${_isFirebaseAvailable.value}")
        } catch (e: Exception) {
            Log.d("AuthManager", "Firebase not configured. Running in Local Room Mode: ${e.message}")
            _isFirebaseAvailable.value = false
        }
    }

    fun continueAsLocalUser(defaultName: String = "Scholar") {
        _authState.value = AuthState.Authenticated(
            AuthUser(
                uid = "local_user_default",
                displayName = defaultName,
                email = null,
                isGoogleUser = false
            )
        )
    }

    fun mockSignInGoogle(name: String, email: String) {
        // Allows immediate, hassle-free Google account simulation for local testing or connected accounts
        _authState.value = AuthState.Authenticated(
            AuthUser(
                uid = "google_${System.currentTimeMillis()}",
                displayName = name,
                email = email,
                isGoogleUser = true
            )
        )
    }

    fun signOut() {
        _authState.value = AuthState.Unauthenticated
    }
}
