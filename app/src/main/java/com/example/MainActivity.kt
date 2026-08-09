package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.data.repository.VideoRepository
import com.example.data.service.DownloadManagerService
import com.example.data.service.DownloadState
import com.example.data.service.LinkParserService
import com.example.ui.components.AmbientGlassBackground
import com.example.ui.components.DhikrReminderBanner
import com.example.ui.components.SaveFlowGlassBottomNav
import com.example.ui.components.SaveFlowTab
import com.example.ui.dialogs.DeleteConfirmDialog
import com.example.ui.dialogs.DownloadProgressOverlay
import com.example.ui.dialogs.QualitySelectionDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.dialogs.VideoPlayerDialog
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.SaveFlowTheme
import com.example.ui.util.AppLanguage
import com.example.ui.util.StringResources
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(applicationContext)
        val linkParser = LinkParserService()
        val downloadManager = DownloadManagerService(applicationContext)
        val repository = VideoRepository(db.videoDao(), linkParser, downloadManager)
        val factory = MainViewModelFactory(repository)

        setContent {
            val mainViewModel: MainViewModel = viewModel(factory = factory)

            val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()
            val language by mainViewModel.appLanguage.collectAsState()
            val autoClear by mainViewModel.autoClearClipboard.collectAsState()

            val isQualityDialogVisible by mainViewModel.isQualityDialogVisible.collectAsState()
            val isSettingsVisible by mainViewModel.isSettingsVisible.collectAsState()
            val parsedMetadata by mainViewModel.parsedMetadata.collectAsState()
            val downloadState by mainViewModel.downloadState.collectAsState()
            val playingVideo by mainViewModel.playingVideo.collectAsState()
            val videoToDelete by mainViewModel.videoToDelete.collectAsState()

            val layoutDirection = if (language == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                SaveFlowTheme(darkTheme = isDarkTheme) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

                    val currentTab = when (currentRoute) {
                        "downloads" -> SaveFlowTab.DOWNLOADS
                        "history" -> SaveFlowTab.HISTORY
                        else -> SaveFlowTab.HOME
                    }

                    AmbientGlassBackground(isDarkTheme = isDarkTheme) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            NavHost(
                                navController = navController,
                                startDestination = "home",
                                modifier = Modifier.fillMaxSize()
                            ) {
                                composable("home") {
                                    HomeScreen(
                                        viewModel = mainViewModel,
                                        onNavigateToDownloads = { navController.navigate("downloads") },
                                        onNavigateToHistory = { navController.navigate("history") }
                                    )
                                }
                                composable("downloads") {
                                    DownloadsScreen(
                                        viewModel = mainViewModel,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                                composable("history") {
                                    HistoryScreen(
                                        viewModel = mainViewModel,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                            }

                            // Glass Floating Bottom Navigation Bar (Visible across all tabs)
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                                modifier = Modifier.align(Alignment.BottomCenter)
                            ) {
                                SaveFlowGlassBottomNav(
                                    currentTab = currentTab,
                                    onTabSelected = { tab ->
                                        when (tab) {
                                            SaveFlowTab.HOME -> navController.navigate("home") {
                                                popUpTo("home") { inclusive = true }
                                            }
                                            SaveFlowTab.DOWNLOADS -> navController.navigate("downloads") {
                                                popUpTo("home")
                                            }
                                            SaveFlowTab.HISTORY -> navController.navigate("history") {
                                                popUpTo("home")
                                            }
                                            SaveFlowTab.SETTINGS -> mainViewModel.openSettings()
                                        }
                                    },
                                    homeTitle = StringResources.getString("nav_home", language),
                                    downloadsTitle = StringResources.getString("nav_downloads", language),
                                    historyTitle = StringResources.getString("nav_history", language),
                                    settingsTitle = StringResources.getString("nav_settings", language)
                                )
                            }

                            // Gentle dhikr reminder - shown briefly each time the app opens
                            DhikrReminderBanner(modifier = Modifier.align(Alignment.BottomCenter))
                        }

                        // Dialog Overlays
                        if (isQualityDialogVisible && parsedMetadata != null) {
                            QualitySelectionDialog(
                                metadata = parsedMetadata!!,
                                language = language,
                                onSelectQuality = { stream ->
                                    mainViewModel.startDownloadWithQuality(stream)
                                },
                                onDismiss = { mainViewModel.dismissQualityDialog() }
                            )
                        }

                        if (downloadState !is DownloadState.Idle) {
                            DownloadProgressOverlay(
                                downloadState = downloadState,
                                language = language,
                                onCancel = { mainViewModel.cancelDownload() },
                                onDismiss = { mainViewModel.resetDownloadState() }
                            )
                        }

                        if (playingVideo != null) {
                            VideoPlayerDialog(
                                video = playingVideo!!,
                                onShare = { mainViewModel.shareVideo(this@MainActivity, playingVideo!!) },
                                onDismiss = { mainViewModel.dismissPlayer() }
                            )
                        }

                        if (isSettingsVisible) {
                            SettingsDialog(
                                language = language,
                                isDarkTheme = isDarkTheme,
                                autoClearClipboard = autoClear,
                                onToggleLanguage = { mainViewModel.toggleLanguage() },
                                onToggleTheme = { mainViewModel.toggleTheme() },
                                onToggleAutoClear = { mainViewModel.toggleAutoClearClipboard() },
                                onDismiss = { mainViewModel.dismissSettings() }
                            )
                        }

                        if (videoToDelete != null) {
                            DeleteConfirmDialog(
                                language = language,
                                onConfirm = { mainViewModel.confirmDelete() },
                                onDismiss = { mainViewModel.dismissDeleteConfirmation() }
                            )
                        }
                    }
                }
            }
        }
    }
}

