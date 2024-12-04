package eu.darken.apl.watch.ui.details

sealed class WatchlistDetailsEvents {
    data class RemovalConfirmation(val id: String) : WatchlistDetailsEvents()
}