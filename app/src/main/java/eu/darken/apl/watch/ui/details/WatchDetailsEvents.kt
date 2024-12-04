package eu.darken.apl.watch.ui.details

sealed class WatchDetailsEvents {
    data class RemovalConfirmation(val id: String) : WatchDetailsEvents()
}