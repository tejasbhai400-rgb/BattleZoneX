package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Tournaments : Screen("tournaments")
    object TournamentDetail : Screen("tournament_detail/{tournamentId}") {
        fun createRoute(tournamentId: String) = "tournament_detail/$tournamentId"
    }
    object Wallet : Screen("wallet")
    object Referral : Screen("referral")
    object Profile : Screen("profile")
    object Admin : Screen("admin")
    object Notifications : Screen("notifications")
}
