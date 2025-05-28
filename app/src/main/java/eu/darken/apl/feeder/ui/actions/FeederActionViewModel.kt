package eu.darken.apl.feeder.ui.actions

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.WebpageTool
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.flow.SingleEventFlow
import eu.darken.apl.common.navigation.navArgs
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.feeder.core.AnywhereTool
import eu.darken.apl.feeder.core.Feeder
import eu.darken.apl.feeder.core.FeederRepo
import eu.darken.apl.feeder.core.ReceiverId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import java.time.Duration
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class FeederActionViewModel @Inject constructor(
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val feederRepo: FeederRepo,
    private val webpageTool: WebpageTool,
    private val anywhereTool: AnywhereTool,
) : ViewModel3(
    dispatcherProvider,
    tag = logTag("Feeder", "Action", "Dialog", "ViewModel"),
) {

    private val navArgs by handle.navArgs<FeederActionDialogArgs>()
    private val feederId: ReceiverId = navArgs.receiverId
    private val trigger = MutableStateFlow(UUID.randomUUID())
    val events = SingleEventFlow<FeederActionEvents>()

    init {
        feederRepo.feeders
            .map { feeders -> feeders.singleOrNull { it.id == feederId } }
            .filter { it == null }
            .take(1)
            .onEach {
                log(tag) { "App data for $feederId is no longer available" }
                popNavStack()
            }
            .launchInViewModel()
    }

    val state = combine(
        trigger,
        feederRepo.feeders.mapNotNull { data -> data.singleOrNull { it.id == feederId } },
    ) { _, feeder ->
        State(
            feeder = feeder,
        )
    }.asStateFlow()

    fun removeFeeder(confirmed: Boolean = false) = launch {
        log(tag) { "removeFeeder()" }
        if (!confirmed) {
            events.emit(FeederActionEvents.RemovalConfirmation(feederId))
            return@launch
        }

        feederRepo.removeFeeder(feederId)
    }

    fun toggleNotifyWhenOffline() = launch {
        log(tag) { "toggleNotifyWhenOffline()" }
        val newTimeout = if (state.first().feeder.config.offlineCheckTimeout != null) {
            null
        } else {
            Duration.ofHours(6)
        }
        feederRepo.setOfflineCheckTimeout(feederId, newTimeout)
    }

    fun showFeedOnMap() = launch {
        log(tag) { "showFeedOnMap()" }
        val feeder = state.first().feeder
        webpageTool.open(
            anywhereTool.createLink(
                ids = setOf(feeder.anywhereId),
                center = feeder.config.position,
            ),
        )
    }

    data class State(
        val feeder: Feeder,
    )
}