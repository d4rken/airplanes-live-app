package eu.darken.apl.feeder.ui.actions

import eu.darken.apl.feeder.core.ReceiverId

sealed class FeederActionEvents {
    data class RemovalConfirmation(val id: ReceiverId) : FeederActionEvents()
}