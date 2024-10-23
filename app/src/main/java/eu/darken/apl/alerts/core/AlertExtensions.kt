import android.content.Context
import eu.darken.apl.R
import eu.darken.apl.main.core.aircraft.Aircraft

fun Aircraft.getBasicAlertNote(context: Context): String {
    val callsignText = "${context.getString(R.string.common_callsign_label)}: ${this.callsign}"
    return "$callsignText\n${description}"
}