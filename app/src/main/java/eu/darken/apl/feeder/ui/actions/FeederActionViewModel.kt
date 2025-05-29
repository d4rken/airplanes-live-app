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
import eu.darken.apl.feeder.core.Feeder
import eu.darken.apl.feeder.core.FeederRepo
import eu.darken.apl.feeder.core.ReceiverId
import eu.darken.apl.map.core.MapOptions
import eu.darken.apl.map.core.toMapFeedId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import java.net.Inet4Address
import java.net.InetAddress
import java.time.Duration
import java.util.UUID
import java.util.regex.Pattern
import javax.inject.Inject


@HiltViewModel
class FeederActionViewModel @Inject constructor(
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val feederRepo: FeederRepo,
    private val webpageTool: WebpageTool,
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

    fun renameFeeder(newName: String? = null) = launch {
        log(tag) { "renameFeeder($newName)" }
        if (newName == null) {
            events.emit(FeederActionEvents.Rename(state.first().feeder))
            return@launch
        }
        feederRepo.setLabel(feederId, newName.takeIf { it.isNotBlank() })
    }

    fun changeAddress(address: String? = null) = launch {
        log(tag) { "changeAddress($address)" }
        if (address == null) {
            events.emit(FeederActionEvents.ChangeIpAddress(state.first().feeder))
            return@launch
        }

        feederRepo.setAddress(
            feederId,
            address.takeIf { it.isNotBlank() }?.let {
                val validIp = InetAddress.getByName(it) is Inet4Address
                val validTld = Pattern.compile("^[a-zA-Z0-9-]{2,256}\\.[a-zA-Z]{2,6}$").matcher(it).matches()
                if (!validIp && !validTld) throw IllegalArgumentException("Invalid address: $address")
                it
            }
        )
    }

    fun showFeedOnMap() = launch {
        log(tag) { "showFeedOnMap()" }
        val feeder = state.first().feeder
        FeederActionDialogDirections.actionFeederActionDialogToMap(
            mapOptions = MapOptions(feeds = setOf(feeder.id.toMapFeedId()))
        ).navigate()
    }

    fun openTar1090() = launch {
        log(tag) { "openTar1090()" }
        val feeder = state.first().feeder
        webpageTool.open("http://${feeder.config.address}/tar1090")
    }

    fun openGraphs1090() = launch {
        log(tag) { "openGraphs1090()" }
        val feeder = state.first().feeder
        webpageTool.open("http://${feeder.config.address}/graphs1090")
    }

    data class State(
        val feeder: Feeder,
    )
}