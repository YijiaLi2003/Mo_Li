package com.example.vocab.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vocab.R
import com.example.vocab.ui.theme.Screen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(auth: FirebaseAuth, navController: NavController) {
    val currentUser = auth.currentUser
    val backgroundImage = if (currentUser == null) {
        R.drawable.unauthenticated // bg for unauthenticated users
    } else {
        R.drawable.authenticated // bg for authenticated users
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp) //Adjust the bg area
            ) {
                Image(
                    painter = painterResource(id = backgroundImage),
                    contentDescription = "Profile Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Main content
            if (currentUser == null) {
                // Show Sign In and Sign Up options if the user is not signed in
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome Back!",
                        style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.secondary)
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { navController.navigate(Screen.SignIn.route) }
                        ) {
                            Text("Sign In", style = MaterialTheme.typography.headlineMedium)
                        }

                        Button(
                            onClick = { navController.navigate(Screen.SignUp.route) }
                        ) {
                            Text("Sign Up", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            } else {
                // Show the profile information if the user is signed in
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome, ${currentUser.email ?: "User"}!",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            auth.signOut()
                            navController.navigate(Screen.Profile.route) {
                                popUpTo(Screen.Profile.route) { inclusive = true }
                            }
                        }
                    ) {
                        Text("Sign Out", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
    }
}
