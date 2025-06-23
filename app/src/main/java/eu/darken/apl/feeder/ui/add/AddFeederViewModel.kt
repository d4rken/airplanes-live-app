package eu.darken.apl.feeder.ui.add

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.flow.SingleEventFlow
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.feeder.core.FeederRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddFeederViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val feederRepo: FeederRepo,
    private val json: Json,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    private val _receiverId = MutableStateFlow("")
    private val _receiverLabel = MutableStateFlow("")
    private val _receiverIpAddress = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)

    val events = SingleEventFlow<AddFeederEvents>()

    init {
        handle.get<String>("qr_data")?.let { qrData ->
            log(tag) { "Processing QR data from SavedStateHandle: $qrData" }
            handleQrScan(qrData)
        }
    }

    val state = combine(
        _receiverId,
        _receiverLabel,
        _receiverIpAddress,
        _isLoading,
    ) { id, label, ipAddress, isLoading ->
        val isValidInput = try {
            UUID.fromString(id.trim())
            id.trim().isNotBlank()
        } catch (e: IllegalArgumentException) {
            false
        }

        State(
            receiverId = id,
            receiverLabel = label,
            receiverIpAddress = ipAddress,
            isAddButtonEnabled = isValidInput && !isLoading,
            isLoading = isLoading,
        )
    }.asStateFlow()

    fun addFeeder() = launch {
        val currentState = state.first()
        log(tag) { "addFeeder($currentState)" }

        _isLoading.value = true

        try {
            UUID.fromString(currentState.receiverId) // ID check

            feederRepo.addFeeder(currentState.receiverId)
            if (currentState.receiverLabel.isNotBlank()) {
                feederRepo.setLabel(currentState.receiverId, currentState.receiverLabel)
            }
            if (currentState.receiverIpAddress.isNotBlank()) {
                feederRepo.setAddress(currentState.receiverId, currentState.receiverIpAddress)
            }
            popNavStack()
        } catch (e: Exception) {
            log(tag) { "Failed to add feeder: ${e.message}" }
        } finally {
            _isLoading.value = false
        }
    }

    fun updateReceiverId(id: String) {
        if (id == _receiverId.value || _isLoading.value) return
        log(tag) { "updateReceiverId($id)" }
        _receiverId.value = id
    }

    fun updateReceiverLabel(label: String) {
        if (label == _receiverLabel.value || _isLoading.value) return
        log(tag) { "updateReceiverLabel($label)" }
        _receiverLabel.value = label
    }

    fun updateReceiverIpAddress(ipAddress: String) {
        if (ipAddress == _receiverIpAddress.value || _isLoading.value) return
        log(tag) { "updateReceiverIpAddress($ipAddress)" }
        _receiverIpAddress.value = ipAddress
    }

    fun handleQrScan(text: String) = launch {
        log(tag) { "handleQrScan($text)" }

        val uri = try {
            text.toUri()
        } catch (_: Exception) {
            log(tag) { "handleQrScan(): Invalid URI format" }
            return@launch
        }

        val feederQR = NewFeederQR.fromUri(uri, json)
        if (feederQR == null) {
            log(tag) { "handleQrScan(): Failed to parse QR data" }
            return@launch
        }

        log(tag) { "handleQrScan(): Got $feederQR" }

        updateReceiverId(feederQR.receiverId)
        updateReceiverLabel(feederQR.receiverLabel ?: "")
        updateReceiverIpAddress(feederQR.receiverIpv4Address ?: "")

        events.tryEmit(AddFeederEvents.StopCamera)
    }


    data class State(
        val receiverId: String,
        val receiverLabel: String,
        val receiverIpAddress: String,
        val isAddButtonEnabled: Boolean,
        val isLoading: Boolean,
    )


}
