package eu.darken.apl.main.ui

import androidx.navigation.NavDirections

sealed interface MainActivityEvents {
    data class BottomNavigation(val directions: NavDirections) : MainActivityEvents
}