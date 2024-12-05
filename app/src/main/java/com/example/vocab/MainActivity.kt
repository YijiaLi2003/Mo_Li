//MainActivity.kt
package com.example.vocab

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vocab.dao.VocabularyDao
import com.example.vocab.database.AppDatabase
import com.example.vocab.screens.*
import com.example.vocab.screens.profile_sub.*
import com.example.vocab.screens.learn_landscape.*
import com.example.vocab.screens.learn_portrait.*
import com.example.vocab.viewmodel.SplashViewModel
import com.google.firebase.auth.FirebaseAuth
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import com.example.vocab.ui.theme.*

@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}



class MainActivity : ComponentActivity() {

    private lateinit var vocabularyDao: VocabularyDao
    private lateinit var auth: FirebaseAuth

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

        setContent {
            Vocab_Theme {
                val splashViewModel: SplashViewModel = viewModel()

                val isSplashVisible by splashViewModel.isSplashVisible.collectAsState()
                val navController = rememberNavController()

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
//                            composable(Screen.Community.route) { CommunityScreen() }
                            composable(Screen.Search.route) { SearchScreen() }
                            composable(Screen.Profile.route) { ProfileScreen(auth, navController) }

                            // Profile Sub-Screens
                            composable(ProfileSubScreen.WordBooks.route) { WordBooksScreen(navController = navController) }
                            composable(ProfileSubScreen.FavouriteWords.route) { FavouriteWordsScreen(navController = navController) }
                            composable(ProfileSubScreen.ReStudyWords.route) { ReStudyWordsScreen(navController = navController) }
                            composable(ProfileSubScreen.WordNotes.route) { WordNotesScreen(navController = navController) }
                            composable(ProfileSubScreen.LearningData.route) { LearningDataScreen(navController = navController) }
                            composable(ProfileSubScreen.Settings.route) { SettingsScreen(navController = navController) }

                            //learning start point
                            composable(Screen.LearningSection.route) { LearningSection(navController = navController) }

                            //LearnInLandscape
                            composable(LearnInLandscape.LandScapeLearn.route) { LearningInLandScreen(navController = navController) }
                            //LearnInPortrait
                            composable(LearnInPortrait.PortraitLearn.route) { LearningInPortScreen(navController = navController) }

                            //Quiz
                            composable(Quiz.QuizTaking.route) { QuizScreen(navController = navController) }
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
