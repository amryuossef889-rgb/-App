package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object SunnahList : Screen("sunnah_list")
    object Search : Screen("search")
    object Library : Screen("library")
    object Settings : Screen("settings")
    object AdminLibrary : Screen("admin_library")
    
    object SunnahDetail : Screen("sunnah_detail/{sunnahId}") {
        fun createRoute(sunnahId: Int) = "sunnah_detail/$sunnahId"
    }

    object PdfReader : Screen("pdf_reader/{bookId}?title={title}&filename={filename}") {
        fun createRoute(bookId: Int, title: String, filename: String) = 
            "pdf_reader/$bookId?title=${android.net.Uri.encode(title)}&filename=${android.net.Uri.encode(filename)}"
    }
}
