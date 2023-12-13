package eu.darken.apl.alerts.ui.actions

sealed class AlertActionEvents {
    data class RemovalConfirmation(val id: String) : AlertActionEvents()
}