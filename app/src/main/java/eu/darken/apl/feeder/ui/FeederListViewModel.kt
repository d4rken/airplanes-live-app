package eu.darken.apl.feeder.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.WebpageTool
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.feeder.core.FeederRepo
import eu.darken.apl.feeder.ui.types.DefaultFeederVH
import eu.darken.apl.map.core.AirplanesLive
import eu.darken.apl.map.core.MapOptions
import eu.darken.apl.map.core.toMapFeedId
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FeederListViewModel @Inject constructor(
    @Suppress("UNUSED_PARAMETER") handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val feederRepo: FeederRepo,
    private val webpageTool: WebpageTool,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

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
        val items = feeders.map { feeder ->
            DefaultFeederVH.Item(
                feeder = feeder,
                onTap = {
                    FeederListFragmentDirections.actionFeederToFeederActionDialog(feeder.id).navigate()
                },
            )
        }
        State(
            items = items,
            isRefreshing = isRefreshing,
        )
    }.asStateFlow()

    fun addFeeder(label: String, rawId: String) = launch {
        log(TAG) { "addFeeder($label,$rawId)" }

        UUID.fromString(rawId) // ID check

        feederRepo.addFeeder(rawId)
        if (label.isNotBlank()) feederRepo.setLabel(rawId, label)
    }

    fun refresh() = launch {
        log(TAG) { "refresh()" }
        feederRepo.refresh()
    }

    fun startFeeding() = launch {
        webpageTool.open(AirplanesLive.URL_START_FEEDING)
    }

    fun showFeedsOnMap(ids: Collection<FeederListAdapter.Item>) = launch {
        log(tag) { "showFeedsOnMap($ids)" }
        val ids = ids
            .filterIsInstance<DefaultFeederVH.Item>()
            .map { it.feeder.id.toMapFeedId() }
            .toSet()
        FeederListFragmentDirections.actionFeederToMap(
            mapOptions = MapOptions(feeds = ids)
        ).navigate()
    }

    data class State(
        val items: List<FeederListAdapter.Item>,
        val isRefreshing: Boolean = false,
    )

    companion object {
        private val TAG = logTag("Feeder", "List", "ViewModel")
    }
}