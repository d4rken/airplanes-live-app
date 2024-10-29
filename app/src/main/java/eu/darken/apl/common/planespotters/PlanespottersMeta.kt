package eu.darken.apl.common.planespotters

import android.content.Context
import eu.darken.apl.R
import eu.darken.apl.main.core.aircraft.AircraftHex

data class PlanespottersMeta(
    val hex: AircraftHex,
    val author: String?,
    val link: String,
) {
    fun getCaption(context: Context): String = if (author != null) {
        context.getString(R.string.thumbnail_caption_by_x, author)
    } else {
        context.getString(R.string.thumbnail_caption_prompt)
    }
}