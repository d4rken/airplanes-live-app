package eu.darken.apl.feeder.ui.add

sealed class AddFeederEvents {
    object StopCamera : AddFeederEvents()
    data class ShowLocalDetectionResult(val result: LocalDetectionResult) : AddFeederEvents()
}

enum class LocalDetectionResult {
    FOUND,
    NOT_FOUND
}