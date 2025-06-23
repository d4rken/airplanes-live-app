package eu.darken.apl.feeder.ui.add

import android.os.Parcelable
import eu.darken.apl.feeder.core.ReceiverId
import kotlinx.parcelize.Parcelize

@Parcelize
data class NewFeederArgs(
    val receiverId: ReceiverId,
    val label: String?,
) : Parcelable