package com.readlater.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.readlater.data.AuthState
import com.readlater.ui.components.BrutalistButton

@Composable
fun SetupScreen(
    authState: AuthState,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "READLATER",
            style = MaterialTheme.typography.displayLarge,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Schedule time for content you discover",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(64.dp))

        when (authState) {
            is AuthState.Loading -> {
                Text(
                    text = "LOADING...",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Black
                )
            }

            is AuthState.NotAuthenticated -> {
                BrutalistButton(
                    text = "Connect Google Calendar",
                    onClick = onConnectClick
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color.Black)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "After connecting, share any link to ReadLater to schedule reading time on your calendar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
            }

            is AuthState.Authenticated -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color.Black)
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "CONNECTED",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = authState.account.email ?: "Unknown",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                BrutalistButton(
                    text = "Disconnect",
                    onClick = onDisconnectClick,
                    filled = false
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color.Black)
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "HOW TO USE",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "1. Find an article or video\n2. Tap Share\n3. Select ReadLater\n4. Pick a date and time\n5. Event created on your calendar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
                        )
                    }
                }
            }

            is AuthState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color.Black)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "ERROR: ${authState.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                BrutalistButton(
                    text = "Try Again",
                    onClick = onConnectClick
                )
            }
        }
    }
}
