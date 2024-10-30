package eu.darken.apl.watchlist.ui.details

sealed class WatchlistDetailsEvents {
    data class RemovalConfirmation(val id: String) : WatchlistDetailsEvents()
}