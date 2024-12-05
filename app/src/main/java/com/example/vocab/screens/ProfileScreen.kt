package com.example.vocab.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.DataThresholding
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vocab.R
import com.example.vocab.ui.theme.ProfileSubScreen
import com.example.vocab.ui.theme.Screen
import com.google.firebase.auth.FirebaseAuth
import com.example.vocab.isLandscape



@Composable
fun ProfileScreen(auth: FirebaseAuth, navController: NavController) {
    var isAuthenticated by rememberSaveable { mutableStateOf(auth.currentUser != null) }
    val isDarkMode = isSystemInDarkTheme()
    val backgroundImage = when {
        isAuthenticated && isDarkMode -> R.drawable.authenticated_dark // bg for authenticated users in dark mode
        isAuthenticated && !isDarkMode -> R.drawable.authenticated_light // bg for authenticated users in light mode
        !isAuthenticated && isDarkMode -> R.drawable.unauthenticated_dark // bg for unauthenticated users in dark mode
        else -> R.drawable.unauthenticated_light // bg for unauthenticated users in light mode
    }
    val isLandscape = isLandscape()
    // Scroll state for the screen
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
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
            if (!isAuthenticated) {
                // Show Sign In and Sign Up options for unauthenticated users
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
                            Text("Sign In", style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.secondary))
                        }
                        Button(
                            onClick = { navController.navigate(Screen.SignUp.route) }
                        ) {
                            Text("Sign Up", style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.secondary))
                        }
                    }
                }
            } else {
                // Show authenticated user profile
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome, ${auth.currentUser?.email ?: "User"}!",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.secondary)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    //Profile page main contents!!!!!!!!!!!!!!!!!!!
                    if (isLandscape){

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {

                                Box(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .padding(end = 8.dp)
                                        .weight(0.5f)
                                        .aspectRatio(1.618f)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { navController.navigate(
                                            ProfileSubScreen.WordBooks.route)},
                                    contentAlignment = Alignment.Center,

                                    ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    )
                                    {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                                            contentDescription = "Book",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Text(
                                            text = "Word Books",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .padding(start = 8.dp)
                                        .weight(0.5f)
                                        .aspectRatio(1.618f)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { navController.navigate(ProfileSubScreen.FavouriteWords.route) },
                                    contentAlignment = Alignment.Center

                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    )
                                    {
                                        Icon(
                                            imageVector = Icons.Outlined.StarOutline,
                                            contentDescription = "Favourite",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Text(
                                            text = "Favourite Words",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .padding(end = 16.dp)
                                        .weight(0.5f)
                                        .aspectRatio(1.618f)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { navController.navigate(ProfileSubScreen.ReStudyWords.route) },
                                    contentAlignment = Alignment.Center

                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    )
                                    {
                                        Icon(
                                            imageVector = Icons.Outlined.Bookmarks,
                                            contentDescription = "Review List",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Text(
                                            text = "Re-Study Words",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {

                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .padding(start = 16.dp)
                                        .weight(0.5f)
                                        .aspectRatio(1.618f)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { navController.navigate(ProfileSubScreen.WordNotes.route) },
                                    contentAlignment = Alignment.Center

                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    )
                                    {
                                        Icon(
                                            imageVector = Icons.Outlined.NoteAlt,
                                            contentDescription = "Word Notes",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Text(
                                            text = "Word Notes",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .padding(end = 8.dp)
                                        .weight(0.5f)
                                        .aspectRatio(1.618f)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { navController.navigate(ProfileSubScreen.LearningData.route) },
                                    contentAlignment = Alignment.Center,

                                    ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    )
                                    {
                                        Icon(
                                            imageVector = Icons.Outlined.DataThresholding,
                                            contentDescription = "learning data",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Text(
                                            text = "Learning Data",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .padding(start = 8.dp)
                                        .weight(0.5f)
                                        .aspectRatio(1.618f)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { navController.navigate(ProfileSubScreen.Settings.route) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    )
                                    {
                                        Icon(
                                            imageVector = Icons.Outlined.Settings,
                                            contentDescription = "settings",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Text(
                                            text = "Settings",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                        }

                    }else{
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {

                                Box(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .padding(end = 8.dp)
                                        .weight(0.5f)
                                        .aspectRatio(1.618f)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { navController.navigate(
                                            ProfileSubScreen.WordBooks.route)},
                                    contentAlignment = Alignment.Center,

                                    ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    )
                                    {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                                            contentDescription = "Book",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Text(
                                            text = "Word Books",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .padding(start = 8.dp)
                                        .weight(0.5f)
                                        .aspectRatio(1.618f)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { navController.navigate(ProfileSubScreen.FavouriteWords.route) },
                                    contentAlignment = Alignment.Center

                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    )
                                    {
                                        Icon(
                                            imageVector = Icons.Outlined.StarOutline,
                                            contentDescription = "Favourite",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Text(
                                            text = "Favourite Words",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .padding(end = 8.dp)
                                        .weight(0.5f)
                                        .aspectRatio(1.618f)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { navController.navigate(ProfileSubScreen.ReStudyWords.route) },
                                    contentAlignment = Alignment.Center

                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    )
                                    {
                                        Icon(
                                            imageVector = Icons.Outlined.Bookmarks,
                                            contentDescription = "Review List",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Text(
                                            text = "Re-Study Words",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .padding(start = 8.dp)
                                        .weight(0.5f)
                                        .aspectRatio(1.618f)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { navController.navigate(ProfileSubScreen.WordNotes.route) },
                                    contentAlignment = Alignment.Center

                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    )
                                    {
                                        Icon(
                                            imageVector = Icons.Outlined.NoteAlt,
                                            contentDescription = "Word Notes",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Text(
                                            text = "Word Notes",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {

                                Box(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .padding(end = 8.dp)
                                        .weight(0.5f)
                                        .aspectRatio(1.618f)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { navController.navigate(ProfileSubScreen.LearningData.route) },
                                    contentAlignment = Alignment.Center,

                                    ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    )
                                    {
                                        Icon(
                                            imageVector = Icons.Outlined.DataThresholding,
                                            contentDescription = "learning data",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Text(
                                            text = "Learning Data",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .padding(start = 8.dp)
                                        .weight(0.5f)
                                        .aspectRatio(1.618f)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { navController.navigate(ProfileSubScreen.Settings.route) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    )
                                    {
                                        Icon(
                                            imageVector = Icons.Outlined.Settings,
                                            contentDescription = "settings",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Text(
                                            text = "Settings",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            auth.signOut()
                            isAuthenticated = false
                        }
                    ) {
                        Text("Sign Out", style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.secondary))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "© 2024 Mo_Li Vocab. All rights reserved.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                }
            }
        }
    }
}
