package eu.darken.apl.map.core

import android.os.Parcelable
import eu.darken.apl.main.core.aircraft.AircraftHex
import kotlinx.parcelize.Parcelize

@Parcelize
data class MapOptions(
    val filter: Filter = Filter(),
    val rendering: Rendering = Rendering(),
    val toggles: Toggles = Toggles(),
) : Parcelable {
    @Parcelize
    data class Filter(
        val icaos: Set<AircraftHex> = emptySet(),
        val noIsolation: Boolean? = null,
    ) : Parcelable

    @Parcelize
    data class Rendering(
        val scale: Float? = 1.1f,
        val iconScale: Float? = 0.7f,
        val labelScale: Float? = 0.7f,
        val sidebarWidth: Int? = null,
    ) : Parcelable

    @Parcelize
    data class Toggles(
        val mobile: Boolean? = true,
    ) : Parcelable

    fun createUrl(baseUrl: String = AirplanesLive.URL_GLOBE): String {
        val urlExtra = StringBuilder()

        filter.apply {
            icaos
                .takeIf { it.isNotEmpty() }
                ?.joinToString(",")
                ?.let {
                    urlExtra.append("&icaoFilter=$it")
                    if (noIsolation == true) urlExtra.append("&noIsolation")
                }
        }

        rendering.apply {
            scale?.let { urlExtra.append("&scale=$it") }
            iconScale?.let { urlExtra.append("&iconScale=$it") }
            labelScale?.let { urlExtra.append("&labelScale=$it") }
            sidebarWidth?.let { urlExtra.append("&sidebarWidth=$it") }
        }

        toggles.apply {

        }

        if (urlExtra.isNotEmpty()) urlExtra.replace(0, 1, "?")

        return if (urlExtra.isEmpty()) baseUrl else "$baseUrl$urlExtra"
    }
}