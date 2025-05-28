package eu.darken.apl.feeder.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.WebpageTool
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.feeder.core.Feeder
import eu.darken.apl.feeder.core.FeederRepo
import eu.darken.apl.feeder.core.ReceiverId
import eu.darken.apl.main.core.GeneralSettings
import eu.darken.apl.main.core.themeState
import eu.darken.apl.map.core.AirplanesLive
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import javax.inject.Inject

@HiltViewModel
class FeederListViewModel @Inject constructor(
    @Suppress("UNUSED_PARAMETER") handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val settings: GeneralSettings,
    private val feederRepo: FeederRepo,
    private val webpageTool: WebpageTool,
) : ViewModel3(
    dispatcherProvider = dispatcherProvider,
    tag = logTag("Feeder", "List", "ViewModel")
) {

    val themeState = settings.themeState.asStateFlow()

    private val refreshTimer = callbackFlow {
        while (isActive) {
            send(Unit)
            delay(1000)
        }
        awaitClose()
    }
    val state = combine(
        refreshTimer,
        feederRepo.feeders,
        feederRepo.isRefreshing
    ) { _, feeders, isRefreshing ->
        State(
            items = feeders.toList(),
            isRefreshing = isRefreshing,
        )
    }.asStateFlow()

    fun addFeeders(rawId: String) = launch {
        log(tag) { "addFeeders($rawId)" }
        rawId
            .let { if (it.contains(",")) it.split(",") else setOf(it) }
            .map { it.trim() }
            .toSet()
            .forEach { id: ReceiverId -> feederRepo.addFeeder(id) }
    }

    fun refresh() = launch {
        log(tag) { "refresh()" }
        feederRepo.refresh()
    }

    fun startFeeding() = launch {
        webpageTool.open(AirplanesLive.URL_START_FEEDING)
    }

    data class State(
        val items: List<Feeder> = emptyList(),
        val isRefreshing: Boolean = false,
    )
}