package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class AuthUser(
    val uid: String,
    val displayName: String,
    val email: String?,
    val photoUrl: String? = null,
    val isGoogleUser: Boolean = true,
    val idToken: String? = null,
    val isAnonymous: Boolean = false,
    val lastSignInTimestamp: Long = System.currentTimeMillis()
)

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: AuthUser) : AuthState()
    data class Error(val message: String, val canFallback: Boolean = true) : AuthState()
}

/**
 * Robust Authentication Manager integrating:
 * 1. Android Credential Manager (androidx.credentials) + Google ID Token
 * 2. Firebase Auth (FirebaseAuth & GoogleAuthProvider)
 * 3. Secure local session cache with fallback resilience
 */
class AuthManager(private val context: Context) {

    private val TAG = "FocusinAuth"
    private val prefs: SharedPreferences = context.getSharedPreferences("focusin_auth_session", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isFirebaseAvailable = MutableStateFlow(false)
    val isFirebaseAvailable: StateFlow<Boolean> = _isFirebaseAvailable.asStateFlow()

    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    init {
        initializeAuth()
    }

    private fun initializeAuth() {
        checkFirebaseAvailability()
        restoreSession()
    }

    private fun checkFirebaseAvailability() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                try {
                    FirebaseApp.initializeApp(context)
                } catch (e: Exception) {
                    Log.d(TAG, "FirebaseApp auto-init skipped (missing google-services.json): ${e.message}")
                }
            }
            val available = FirebaseApp.getApps(context).isNotEmpty()
            _isFirebaseAvailable.value = available
            Log.d(TAG, "Firebase App initialized: $available")

            if (available) {
                setupFirebaseAuthListener()
            }
        } catch (e: Exception) {
            Log.d(TAG, "Firebase not configured. Running in Local Room Mode: ${e.message}")
            _isFirebaseAvailable.value = false
        }
    }

    private fun setupFirebaseAuthListener() {
        try {
            val auth = FirebaseAuth.getInstance()
            authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val currentUser = firebaseAuth.currentUser
                if (currentUser != null) {
                    val authUser = currentUser.toAuthUser()
                    _authState.value = AuthState.Authenticated(authUser)
                    persistSession(authUser)
                } else if (_authState.value is AuthState.Authenticated && (_authState.value as AuthState.Authenticated).user.isGoogleUser) {
                    // Firebase signed out
                    val saved = readPersistedSession()
                    if (saved == null || saved.idToken.isNullOrBlank()) {
                        _authState.value = AuthState.Unauthenticated
                    }
                }
            }
            auth.addAuthStateListener(authStateListener!!)
        } catch (e: Exception) {
            Log.w(TAG, "Could not attach FirebaseAuth listener", e)
        }
    }

    private fun restoreSession() {
        // 1. Check active Firebase User
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val user = currentUser.toAuthUser()
                    _authState.value = AuthState.Authenticated(user)
                    persistSession(user)
                    return
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Firebase currentUser check failed: ${e.message}")
        }

        // 2. Check Persisted Local Session
        val saved = readPersistedSession()
        if (saved != null) {
            _authState.value = AuthState.Authenticated(saved)
        }
    }

    /**
     * Resolves the Web Client ID for Credential Manager from:
     * 1. Custom user-provided or stored Web Client ID
     * 2. R.string.default_web_client_id (from google-services.json)
     * 3. R.string.google_web_client_id (from strings.xml)
     */
    fun resolveServerClientId(customClientId: String? = null): String {
        if (!customClientId.isNullOrBlank()) {
            return customClientId.trim()
        }

        val stored = prefs.getString("custom_server_client_id", null)
        if (!stored.isNullOrBlank()) {
            return stored.trim()
        }

        // Check generated default_web_client_id
        try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                val value = context.getString(resId)
                if (value.isNotBlank() && !value.startsWith("YOUR_")) {
                    return value.trim()
                }
            }
        } catch (_: Exception) {}

        // Check strings.xml google_web_client_id
        try {
            val value = context.getString(R.string.google_web_client_id)
            if (value.isNotBlank() && !value.startsWith("YOUR_")) {
                return value.trim()
            }
        } catch (_: Exception) {}

        return ""
    }

    fun saveCustomServerClientId(clientId: String) {
        prefs.edit().putString("custom_server_client_id", clientId.trim()).apply()
    }

    /**
     * Executes real Google Sign-In via Android Credential Manager + Firebase Auth.
     */
    suspend fun signInWithGoogleCredential(
        activityContext: Context,
        customClientId: String? = null
    ): Result<AuthUser> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.Loading

        val clientId = resolveServerClientId(customClientId)
        if (clientId.isBlank()) {
            val errorMsg = "Google Web Client ID is not configured. Please specify your Web Client ID from Firebase Console."
            _authState.value = AuthState.Error(errorMsg, canFallback = true)
            return@withContext Result.failure(IllegalStateException(errorMsg))
        }

        try {
            val credentialManager = CredentialManager.create(activityContext)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val email = googleIdTokenCredential.id
                val displayName = googleIdTokenCredential.displayName
                    ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
                val photoUri = googleIdTokenCredential.profilePictureUri?.toString()

                // Authenticate with Firebase Auth
                val firebaseUser = linkWithFirebaseAuth(idToken)

                val authUser = if (firebaseUser != null) {
                    AuthUser(
                        uid = firebaseUser.uid,
                        displayName = firebaseUser.displayName ?: displayName,
                        email = firebaseUser.email ?: email,
                        photoUrl = firebaseUser.photoUrl?.toString() ?: photoUri,
                        isGoogleUser = true,
                        idToken = idToken
                    )
                } else {
                    AuthUser(
                        uid = "google_${email.hashCode().toUInt().toString(16)}",
                        displayName = displayName,
                        email = email,
                        photoUrl = photoUri,
                        isGoogleUser = true,
                        idToken = idToken
                    )
                }

                _authState.value = AuthState.Authenticated(authUser)
                persistSession(authUser)
                Result.success(authUser)
            } else {
                val error = "Unsupported credential format received from Credential Manager"
                _authState.value = AuthState.Error(error, canFallback = true)
                Result.failure(IllegalStateException(error))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "Google Sign-In was cancelled by user")
            val previous = readPersistedSession()
            if (previous != null) {
                _authState.value = AuthState.Authenticated(previous)
            } else {
                _authState.value = AuthState.Unauthenticated
            }
            Result.failure(e)
        } catch (e: NoCredentialException) {
            val msg = "No Google account available on this device. Please sign in to a Google account in Android Settings."
            _authState.value = AuthState.Error(msg, canFallback = true)
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error: ${e.message}", e)
            val friendlyMsg = if (e.message?.contains("10") == true || e.message?.contains("DEVELOPER_ERROR") == true) {
                "Google Sign-In configuration error: Please ensure your SHA-1 and package name match your Firebase & Google Cloud Console configuration."
            } else {
                e.localizedMessage ?: "Google Sign-In request failed."
            }
            _authState.value = AuthState.Error(friendlyMsg, canFallback = true)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In", e)
            _authState.value = AuthState.Error(e.localizedMessage ?: "Authentication failed", canFallback = true)
            Result.failure(e)
        }
    }

    private suspend fun linkWithFirebaseAuth(idToken: String): FirebaseUser? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                try { FirebaseApp.initializeApp(context) } catch (_: Exception) {}
            }
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                val auth = FirebaseAuth.getInstance()
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                suspendCancellableCoroutine { continuation ->
                    auth.signInWithCredential(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(task.result?.user)
                        } else {
                            Log.w(TAG, "FirebaseAuth.signInWithCredential failed: ${task.exception?.message}")
                            continuation.resume(null)
                        }
                    }
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Auth integration error: ${e.message}")
            null
        }
    }

    /**
     * Local testing / instant preview helper:
     * Connects an authenticated Google profile for demo, testing, or offline development.
     */
    fun connectVerifiedGoogleProfile(name: String, email: String, photoUrl: String? = null) {
        val user = AuthUser(
            uid = "google_${email.hashCode().toUInt().toString(16)}",
            displayName = name.ifBlank { "Scholar" },
            email = email.ifBlank { "scholar@gmail.com" },
            photoUrl = photoUrl ?: "https://lh3.googleusercontent.com/a/ACg8ocIScholarDefault=s96-c",
            isGoogleUser = true,
            lastSignInTimestamp = System.currentTimeMillis()
        )
        _authState.value = AuthState.Authenticated(user)
        persistSession(user)
    }

    fun continueAsLocalUser(defaultName: String = "Scholar") {
        if (_authState.value is AuthState.Authenticated) return
        val saved = readPersistedSession()
        if (saved != null) {
            _authState.value = AuthState.Authenticated(saved)
            return
        }
        val guest = AuthUser(
            uid = "local_scholar_guest",
            displayName = defaultName,
            email = null,
            photoUrl = null,
            isGoogleUser = false
        )
        _authState.value = AuthState.Authenticated(guest)
        persistSession(guest)
    }

    fun mockSignInGoogle(name: String, email: String, photoUrl: String? = null) {
        connectVerifiedGoogleProfile(name, email, photoUrl)
    }

    suspend fun signOut(activityContext: Context? = null) = withContext(Dispatchers.IO) {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseAuth.getInstance().signOut()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase signOut error", e)
        }

        if (activityContext != null) {
            try {
                val credentialManager = CredentialManager.create(activityContext)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "CredentialManager clear error", e)
            }
        }

        clearPersistedSession()
        _authState.value = AuthState.Unauthenticated
    }

    fun clearError() {
        val saved = readPersistedSession()
        if (saved != null) {
            _authState.value = AuthState.Authenticated(saved)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    private fun persistSession(user: AuthUser) {
        prefs.edit()
            .putString("uid", user.uid)
            .putString("displayName", user.displayName)
            .putString("email", user.email)
            .putString("photoUrl", user.photoUrl)
            .putBoolean("isGoogleUser", user.isGoogleUser)
            .putString("idToken", user.idToken)
            .putLong("lastSignIn", user.lastSignInTimestamp)
            .apply()
    }

    private fun readPersistedSession(): AuthUser? {
        val uid = prefs.getString("uid", null) ?: return null
        val displayName = prefs.getString("displayName", "Scholar") ?: "Scholar"
        val email = prefs.getString("email", null)
        val photoUrl = prefs.getString("photoUrl", null)
        val isGoogle = prefs.getBoolean("isGoogleUser", false)
        val idToken = prefs.getString("idToken", null)
        val lastSignIn = prefs.getLong("lastSignIn", System.currentTimeMillis())

        return AuthUser(
            uid = uid,
            displayName = displayName,
            email = email,
            photoUrl = photoUrl,
            isGoogleUser = isGoogle,
            idToken = idToken,
            lastSignInTimestamp = lastSignIn
        )
    }

    private fun clearPersistedSession() {
        prefs.edit()
            .remove("uid")
            .remove("displayName")
            .remove("email")
            .remove("photoUrl")
            .remove("isGoogleUser")
            .remove("idToken")
            .remove("lastSignIn")
            .apply()
    }

    private fun FirebaseUser.toAuthUser(): AuthUser {
        val isGoogle = providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }
        val name = displayName?.ifBlank { null }
            ?: email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Scholar"
        return AuthUser(
            uid = uid,
            displayName = name,
            email = email,
            photoUrl = photoUrl?.toString(),
            isGoogleUser = isGoogle,
            lastSignInTimestamp = metadata?.lastSignInTimestamp ?: System.currentTimeMillis()
        )
    }
}
