package com.example.vocab

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.vocab.dao.VocabularyDao
import com.example.vocab.database.AppDatabase
import com.example.vocab.screens.*
import com.example.vocab.screens.profile_sub.*
import com.example.vocab.screens.learn_landscape.*
import com.example.vocab.screens.learn_portrait.*
import com.example.vocab.screens.settings_sub.NotificationSubScreen
import com.example.vocab.viewmodel.SplashViewModel
import com.google.firebase.auth.FirebaseAuth
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import com.example.vocab.ui.theme.*
import com.example.vocab.viewmodel.LearningSectionViewModel
import com.example.vocab.viewmodel.SettingsViewModel
import com.example.vocab.workers.SyncManager

@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

class MainActivity : ComponentActivity() {

    private lateinit var vocabularyDao: VocabularyDao
    private lateinit var auth: FirebaseAuth

    // We track if we asked for notification permission
    private var askedNotificationPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize DAOs
        val database = AppDatabase.getDatabase(this)
        vocabularyDao = database.vocabularyDao()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Import the vocabulary data
        lifecycleScope.launch {
            importVocabularyFromCsv()
        }

        // Schedule the periodic data synchronization
        SyncManager.schedulePeriodicSync(this)

        // Request notification permission if on Android 13+ and not granted yet
        // We'll request it after we set content, so we have a UI if needed
        // Alternatively, you can request right away.

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val settings by settingsViewModel.settingsFlow.collectAsState()
            val darkModeEnabled = settings.darkMode

            Vocab_Theme(darkTheme = darkModeEnabled) {
                val splashViewModel: SplashViewModel = viewModel()

                val isSplashVisible by splashViewModel.isSplashVisible.collectAsState()
                val navController = rememberNavController()

                val learningViewModel: LearningSectionViewModel = viewModel()

                // Launcher for requesting POST_NOTIFICATIONS permission
                val requestPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        if (!isGranted) {
                            Toast.makeText(this, "Notification permission denied. Notifications won't appear.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Notification permission granted.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // Check and request notification permission
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED && !askedNotificationPermission) {
                            askedNotificationPermission = true
                            // Show a rationale if needed
                            // If rationale needed, show a dialog before calling requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                if (isSplashVisible) {
                    SplashScreen()
                } else {
                    val currentUser = auth.currentUser

                    // Define the routes where the bottom navigation bar should be shown
                    val showBottomNavRoutes = listOf(
                        Screen.Home.route,
                        Screen.Search.route,
                        Screen.Profile.route
                    )

                    // Get the current route
                    val currentBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = currentBackStackEntry?.destination?.route

                    Scaffold(
                        bottomBar = {
                            if (currentRoute in showBottomNavRoutes) {
                                BottomNavigationBar(navController = navController)
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = if (currentUser == null) Screen.SignIn.route else Screen.Home.route,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            // Authentication Screens
                            composable(Screen.SignIn.route) {
                                SignInScreen(navController, auth)
                            }
                            composable(Screen.SignUp.route) {
                                SignUpScreen(navController, auth)
                            }

                            // Main App Screens
                            composable(Screen.Home.route) { MainScreen(navController = navController) }
                            composable(Screen.Search.route) { SearchScreen(navController = navController) }
                            composable(Screen.Profile.route) { ProfileScreen(auth, navController) }

                            // Profile Sub-Screens
                            composable(ProfileSubScreen.WordBooks.route) { WordBooksScreen(navController = navController) }
                            composable(ProfileSubScreen.FavouriteWords.route) { FavoriteWordsScreen(navController = navController) }
                            composable(ProfileSubScreen.ReStudyWords.route) { ReStudyWordsScreen(navController = navController) }
                            composable(ProfileSubScreen.LearningData.route) { LearningDataScreen(navController = navController) }
                            composable(ProfileSubScreen.Settings.route) { SettingsScreen(navController = navController) }

                            composable(Screen.LearningSection.route) {
                                LearningSection(navController = navController, learningViewModel = learningViewModel)
                            }

                            composable(LearnInLandscape.LandScapeLearn.route) {
                                LearningInLandScreen(navController = navController, learningViewModel = learningViewModel)
                            }

                            composable(LearnInPortrait.PortraitLearn.route) {
                                LearningInPortScreen(navController = navController, learningViewModel = learningViewModel)
                            }

                            //Quiz
                            composable(Quiz.QuizTaking.route) { QuizScreen(navController = navController) }

                            // notification sub screen
                            composable(SettingsSubScreen.NotificationSub.route) { NotificationSubScreen(navController = navController) }
                        }
                    }

                }
            }
        }

    }

    private suspend fun importVocabularyFromCsv() {
        withContext(Dispatchers.IO) {
            val existingCount = vocabularyDao.getCount()
            if (existingCount > 0) {
                // Data already imported
                return@withContext
            }

            try {
                val inputStream = assets.open("vocabulary.csv")
                val reader = CSVReaderBuilder(InputStreamReader(inputStream, "UTF-8"))
                    .withCSVParser(
                        CSVParserBuilder()
                            .withSeparator(',')
                            .withQuoteChar('"')
                            .withEscapeChar('\\')
                            .build()
                    )
                    .build()

                var nextLine: Array<String>?
                val vocabularyList = mutableListOf<com.example.vocab.model.Vocabulary>()
                while (true) {
                    nextLine = reader.readNext()
                    if (nextLine == null) {
                        break
                    }
                    if (nextLine.isEmpty()) continue

                    // The first field is the word
                    val word = nextLine[0].trim()
                    // The second field is the translation, possibly containing newlines
                    val translation = nextLine.getOrNull(1)?.trim() ?: ""

                    val vocabulary = com.example.vocab.model.Vocabulary(
                        word = word,
                        translation = translation
                    )
                    vocabularyList.add(vocabulary)
                }

                reader.close()

                // Insert all vocabulary into database
                vocabularyDao.insertAllVocabulary(vocabularyList)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
