package com.readlater

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.readlater.data.AuthRepository
import com.readlater.ui.screens.SetupScreen
import com.readlater.ui.theme.ReadLaterTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var authRepository: AuthRepository

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Always handle the result - Google Sign-In doesn't always return RESULT_OK
        authRepository.handleSignInResult(result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authRepository = AuthRepository(applicationContext)

        setContent {
            ReadLaterTheme {
                val authState by authRepository.authState.collectAsState()
                val scope = rememberCoroutineScope()

                SetupScreen(
                    authState = authState,
                    onConnectClick = {
                        signInLauncher.launch(authRepository.getSignInIntent())
                    },
                    onDisconnectClick = {
                        scope.launch {
                            authRepository.signOut()
                            Toast.makeText(
                                this@MainActivity,
                                "Disconnected",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }
    }
}
