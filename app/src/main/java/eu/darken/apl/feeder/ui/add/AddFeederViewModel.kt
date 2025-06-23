package eu.darken.apl.feeder.ui.add

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.feeder.core.FeederRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddFeederViewModel @Inject constructor(
    @Suppress("UNUSED_PARAMETER") handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val feederRepo: FeederRepo,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    private val _receiverId = MutableStateFlow("")
    private val _receiverLabel = MutableStateFlow("")

    val state = combine(
        _receiverId,
        _receiverLabel,
    ) { id, label ->
        State(
            receiverId = id,
            receiverLabel = label,
        )
    }.asStateFlow()

    fun addFeeder(label: String, rawId: String) = launch {
        log(TAG) { "addFeeder($label,$rawId)" }

        UUID.fromString(rawId) // ID check

        feederRepo.addFeeder(rawId)
        if (label.isNotBlank()) feederRepo.setLabel(rawId, label)
    }

    fun handleQrScan(text: String) = launch {
        log(TAG) { "handleQrScan($text)" }
        if (!text.isNotBlank() || !text.startsWith(NewFeederQR.PREFIX)) return@launch

        val jsonData = text.substringAfter("data=")
        val feederQR = Json.decodeFromString<NewFeederQR>(jsonData)

        _receiverId.value = feederQR.receiverId
        _receiverLabel.value = feederQR.receiverLabel ?: ""
    }

    data class State(
        val receiverId: String,
        val receiverLabel: String,
    )


    companion object {
        private val TAG = logTag("Feeder", "Add", "ViewModel")
    }
}
