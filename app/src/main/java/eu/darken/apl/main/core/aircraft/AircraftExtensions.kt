package eu.darken.apl.main.core.aircraft

val Aircraft.messageTypeLabel: String
    get() = when {
        messageType == "mlat" -> "MLAT"
        messageType.startsWith("adsb") -> "ADS-B"
        messageType == "mode_s" -> "MODE-S"
        else -> "Other"
    }
