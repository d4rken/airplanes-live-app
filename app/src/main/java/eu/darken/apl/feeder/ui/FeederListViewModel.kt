package eu.darken.apl.feeder.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.WebpageTool
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.datastore.value
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.feeder.core.FeederRepo
import eu.darken.apl.feeder.core.config.FeederSettings
import eu.darken.apl.feeder.core.config.FeederSortMode
import eu.darken.apl.feeder.ui.types.DefaultFeederVH
import eu.darken.apl.feeder.ui.types.FeederHeaderVH
import eu.darken.apl.map.core.AirplanesLive
import eu.darken.apl.map.core.MapOptions
import eu.darken.apl.map.core.toMapFeedId
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
    private val feederRepo: FeederRepo,
    private val webpageTool: WebpageTool,
    private val feederSettings: FeederSettings,
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
        feederRepo.isRefreshing,
        feederSettings.feederSortMode.flow
    ) { _, feeders, isRefreshing, sortMode ->
        val offlineStates = feeders.associate { it.id to feederRepo.isOffline(it) }

        val sortedFeeders = when (sortMode) {
            FeederSortMode.BY_LABEL -> feeders.sortedBy { it.label }
            FeederSortMode.BY_MESSAGE_RATE -> feeders.sortedByDescending { it.beastMessageRate }
        }

        val feederItems = sortedFeeders.map { feeder ->
            DefaultFeederVH.Item(
                feeder = feeder,
                isOffline = offlineStates[feeder.id]!!,
                onTap = {
                    FeederListFragmentDirections.actionFeederToFeederActionDialog(feeder.id).navigate()
                },
            )
        }

        val headerItem = FeederHeaderVH.Item(
            hasOfflineFeeders = offlineStates.values.any { it },
            currentSortMode = sortMode,
            onSortModeSelected = { newSortMode ->
                launch {
                    feederSettings.feederSortMode.value(newSortMode)
                }
            }
        )

        val allItems = listOf(headerItem) + feederItems

        State(
            items = allItems,
            feederCount = feederItems.size,
            isRefreshing = isRefreshing,
        )
    }.asStateFlow()

    fun refresh() = launch {
        log(TAG) { "refresh()" }
        feederRepo.refresh()
    }

    fun startFeeding() = launch {
        webpageTool.open(AirplanesLive.URL_START_FEEDING)
    }

    fun showFeedsOnMap(items: Collection<FeederListAdapter.Item>) = launch {
        log(TAG) { "showFeedsOnMap($items)" }
        val ids = items
            .filterIsInstance<DefaultFeederVH.Item>()
            .map { it.feeder.id.toMapFeedId() }
            .toSet()
        FeederListFragmentDirections.actionFeederToMap(
            mapOptions = MapOptions(feeds = ids)
        ).navigate()
    }

    data class State(
        val items: List<FeederListAdapter.Item>,
        val feederCount: Int,
        val isRefreshing: Boolean = false,
    )

    companion object {
        private val TAG = logTag("Feeder", "List", "ViewModel")
    }
}
