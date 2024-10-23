package eu.darken.apl.alerts.ui.types

import android.text.format.DateUtils
import android.view.ViewGroup
import androidx.core.view.isGone
import eu.darken.apl.R
import eu.darken.apl.alerts.core.types.AircraftAlert
import eu.darken.apl.alerts.core.types.CallsignAlert
import eu.darken.apl.alerts.core.types.HexAlert
import eu.darken.apl.alerts.core.types.SquawkAlert
import eu.darken.apl.alerts.ui.AlertsListAdapter
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.common.planespotters.PlanespottersMeta
import eu.darken.apl.databinding.AlertsListMultiItemBinding
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class MultiAircraftAlertVH(parent: ViewGroup) :
    AlertsListAdapter.BaseVH<MultiAircraftAlertVH.Item, AlertsListMultiItemBinding>(
        R.layout.alerts_list_multi_item,
        parent
    ), BindableVH<MultiAircraftAlertVH.Item, AlertsListMultiItemBinding> {

    override val viewBinding = lazy { AlertsListMultiItemBinding.bind(itemView) }

    override val onBindData: AlertsListMultiItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        val status = item.status

        title.text = when (status) {
            is HexAlert.Status -> throw IllegalArgumentException("VH does not support hex alerts")
            is CallsignAlert.Status -> throw IllegalArgumentException("VH does not support callsign alerts")
            is SquawkAlert.Status -> status.squawk.uppercase()
        }

        val lastPing = status.tracked.maxOfOrNull { it.seenAt } ?: status.lastHit?.checkAt

        lastTriggered.text = lastPing?.let {
            DateUtils.getRelativeTimeSpanString(
                it.toEpochMilli(),
                Instant.now().toEpochMilli(),
                DateUtils.MINUTE_IN_MILLIS
            ).toString()
        } ?: ""

        alertStatus.apply {
            text = when {
                status.tracked.isNotEmpty() -> getString(R.string.alerts_aircraft_spotted)

                status.lastHit != null -> {
                    val text = status.lastHit!!.checkAt.atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
                    getString(R.string.alerts_aircraft_last_spotted, text)
                }

                else -> getString(R.string.alerts_aircraft_not_spotted)
            }
            setTextColor(
                when {
                    status.tracked.isNotEmpty() -> getColorForAttr(com.google.android.material.R.attr.colorPrimary)
                    status.lastCheck != null -> getColorForAttr(com.google.android.material.R.attr.colorSecondary)
                    else -> getColorForAttr(com.google.android.material.R.attr.colorError)
                }
            )
        }

        noteBox.isGone = status.note.isBlank()
        noteValue.text = status.note

        root.apply {
            setOnClickListener { item.onTap(item) }
        }
    }

    data class Item(
        val status: AircraftAlert.Status,
        val onTap: (Item) -> Unit,
        val onThumbnail: (PlanespottersMeta) -> Unit,
    ) : AlertsListAdapter.Item {
        override val stableId: Long
            get() = status.id.hashCode().toLong()
    }
}