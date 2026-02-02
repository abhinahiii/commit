package com.readlater.data

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(private val context: Context) {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(CalendarScopes.CALENDAR_EVENTS))
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    init {
        checkExistingAuth()
    }

    private fun checkExistingAuth() {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null && hasCalendarScope(account)) {
            _authState.value = AuthState.Authenticated(account)
        } else {
            _authState.value = AuthState.NotAuthenticated
        }
    }

    private fun hasCalendarScope(account: GoogleSignInAccount): Boolean {
        return GoogleSignIn.hasPermissions(account, Scope(CalendarScopes.CALENDAR_EVENTS))
    }

    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    fun launchSignIn(launcher: ActivityResultLauncher<Intent>) {
        launcher.launch(googleSignInClient.signInIntent)
    }

    fun handleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                val account = task.result
                if (account != null) {
                    _authState.value = AuthState.Authenticated(account)
                } else {
                    _authState.value = AuthState.Error("No account returned")
                }
            } else {
                val exception = task.exception
                _authState.value = AuthState.Error(exception?.message ?: "Sign in failed")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Sign in failed")
        }
    }

    suspend fun signOut() {
        googleSignInClient.signOut()
        _authState.value = AuthState.NotAuthenticated
    }

    fun getAccount(): GoogleSignInAccount? {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.account
            else -> null
        }
    }
}

sealed class AuthState {
    data object Loading : AuthState()
    data object NotAuthenticated : AuthState()
    data class Authenticated(val account: GoogleSignInAccount) : AuthState()
    data class Error(val message: String) : AuthState()
}
