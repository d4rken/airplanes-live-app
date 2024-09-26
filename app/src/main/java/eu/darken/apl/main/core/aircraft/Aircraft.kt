package eu.darken.apl.main.core.aircraft

interface Aircraft {
    val hex: String
    val registration: String?
    val flight: String?
    val squawk: String?
    val description: String?
    val altitude: String?

    val id: String
        get() = registration ?: hex

    val label: String
        get() = registration ?: hex
}
