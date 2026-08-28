package com.example.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.SunnahApplication
import com.example.ui.components.AppBackground
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.screens.detail.SunnahDetailScreen
import com.example.ui.screens.detail.SunnahDetailViewModel
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.library.AdminLibraryScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.library.LibraryViewModel
import com.example.ui.screens.library.PdfReaderScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.search.SearchViewModel
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.sunnahs.SunnahListScreen
import com.example.ui.screens.sunnahs.SunnahListViewModel

@Composable
fun AppNavigation(
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as SunnahApplication
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val settings by settingsViewModel.settings.collectAsState()

    // Request Notification permission on Android 13+ (API 33+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val isTopLevelRoute = currentRoute in listOf(
        Screen.Home.route,
        Screen.SunnahList.route,
        Screen.Search.route,
        Screen.Library.route,
        Screen.Settings.route
    )

    val topBarTitle = when {
        currentRoute == Screen.Home.route -> "سُنّة النَّبِيِّ ﷺ"
        currentRoute == Screen.SunnahList.route -> "قائمة السنن الـ 100"
        currentRoute == Screen.Search.route -> "البحث في الأحاديث"
        currentRoute == Screen.Library.route -> "مكتبة الكتب الفقهية"
        currentRoute == Screen.Settings.route -> "الإعدادات والمظهر"
        currentRoute == Screen.AdminLibrary.route -> "لوحة إدارة الكتب"
        currentRoute?.startsWith("sunnah_detail") == true -> "تفاصيل السُنّة النبوية"
        currentRoute?.startsWith("pdf_reader") == true -> {
            val titleArg = navBackStackEntry?.arguments?.getString("title") ?: "قارئ الكتب"
            android.net.Uri.decode(titleArg)
        }
        else -> "سُنّة"
    }

    AppBackground(
        mode = settings.backgroundMode,
        customPath = settings.customBackgroundPath,
        opacity = settings.backgroundOpacity,
        scale = settings.backgroundScale
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = topBarTitle,
                    canNavigateBack = !isTopLevelRoute,
                    onNavigateBack = { navController.navigateUp() }
                )
            },
            bottomBar = {
                if (isTopLevelRoute) {
                    AppBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { targetRoute ->
                            navController.navigate(targetRoute) {
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            },
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    // Home
                    composable(Screen.Home.route) {
                        val homeViewModel: HomeViewModel = viewModel(
                            factory = HomeViewModel.provideFactory(app.sunnahRepository)
                        )
                        HomeScreen(
                            viewModel = homeViewModel,
                            onNavigateToSunnahDetail = { sunnahId ->
                                navController.navigate(Screen.SunnahDetail.createRoute(sunnahId))
                            },
                            onNavigateToSunnahList = {
                                navController.navigate(Screen.SunnahList.route)
                            }
                        )
                    }

                    // Sunnahs List
                    composable(Screen.SunnahList.route) {
                        val sunnahListViewModel: SunnahListViewModel = viewModel(
                            factory = SunnahListViewModel.provideFactory(app.sunnahRepository)
                        )
                        SunnahListScreen(
                            viewModel = sunnahListViewModel,
                            onNavigateToSunnahDetail = { sunnahId ->
                                navController.navigate(Screen.SunnahDetail.createRoute(sunnahId))
                            }
                        )
                    }

                    // Sunnah Detail
                    composable(
                        route = Screen.SunnahDetail.route,
                        arguments = listOf(navArgument("sunnahId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val sunnahId = backStackEntry.arguments?.getInt("sunnahId") ?: 1
                        val detailViewModel: SunnahDetailViewModel = viewModel(
                            factory = SunnahDetailViewModel.provideFactory(sunnahId, app.sunnahRepository)
                        )
                        SunnahDetailScreen(
                            viewModel = detailViewModel,
                            onNavigateBack = { navController.navigateUp() }
                        )
                    }

                    // Search
                    composable(Screen.Search.route) {
                        val searchViewModel: SearchViewModel = viewModel(
                            factory = SearchViewModel.provideFactory(app.sunnahRepository)
                        )
                        SearchScreen(
                            viewModel = searchViewModel
                        )
                    }

                    // Library
                    composable(Screen.Library.route) {
                        val libraryViewModel: LibraryViewModel = viewModel(
                            factory = LibraryViewModel.provideFactory(app.sunnahRepository)
                        )
                        LibraryScreen(
                            viewModel = libraryViewModel,
                            onNavigateToPdfReader = { bookId, title, filename ->
                                navController.navigate(Screen.PdfReader.createRoute(bookId, title, filename))
                            },
                            onNavigateToAdmin = {
                                navController.navigate(Screen.AdminLibrary.route)
                            }
                        )
                    }

                    // Admin Library
                    composable(Screen.AdminLibrary.route) {
                        val libraryViewModel: LibraryViewModel = viewModel(
                            factory = LibraryViewModel.provideFactory(app.sunnahRepository)
                        )
                        AdminLibraryScreen(
                            viewModel = libraryViewModel,
                            onNavigateBack = { navController.navigateUp() }
                        )
                    }

                    // PDF Reader
                    composable(
                        route = Screen.PdfReader.route,
                        arguments = listOf(
                            navArgument("bookId") { type = NavType.IntType },
                            navArgument("title") { type = NavType.StringType; defaultValue = "" },
                            navArgument("filename") { type = NavType.StringType; defaultValue = "" }
                        )
                    ) { backStackEntry ->
                        val bookId = backStackEntry.arguments?.getInt("bookId") ?: 0
                        val rawTitle = backStackEntry.arguments?.getString("title") ?: ""
                        val title = android.net.Uri.decode(rawTitle)
                        val rawFilename = backStackEntry.arguments?.getString("filename") ?: ""
                        val filename = android.net.Uri.decode(rawFilename)

                        PdfReaderScreen(
                            bookId = bookId,
                            title = title,
                            filename = filename,
                            onNavigateBack = { navController.navigateUp() }
                        )
                    }

                    // Settings
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            viewModel = settingsViewModel
                        )
                    }
                }
            }
        }
    }
}
