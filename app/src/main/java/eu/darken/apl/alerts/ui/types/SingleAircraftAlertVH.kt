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
import eu.darken.apl.common.planespotters.load
import eu.darken.apl.databinding.AlertsListSingleItemBinding
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class SingleAircraftAlertVH(parent: ViewGroup) :
    AlertsListAdapter.BaseVH<SingleAircraftAlertVH.Item, AlertsListSingleItemBinding>(
        R.layout.alerts_list_single_item,
        parent
    ), BindableVH<SingleAircraftAlertVH.Item, AlertsListSingleItemBinding> {

    override val viewBinding = lazy { AlertsListSingleItemBinding.bind(itemView) }

    override val onBindData: AlertsListSingleItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        val status = item.status

        when (status) {
            is HexAlert.Status -> {
                title.text = status.hex.uppercase()
                subtitle.text = getString(R.string.alerts_item_hexcode_subtitle)
                alertIcon.setImageResource(R.drawable.ic_hexagon_multiple_24)
            }

            is CallsignAlert.Status -> {
                title.text = status.callsign.uppercase()
                subtitle.text = getString(R.string.alerts_item_callsign_subtitle)
                alertIcon.setImageResource(R.drawable.ic_bullhorn_24)
            }

            is SquawkAlert.Status -> {
                throw IllegalArgumentException("Wrong VH for SQUAWK")
            }
        }

        lastTriggered.apply {
            val lastPing = status.tracked.maxOfOrNull { it.seenAt } ?: status.lastHit?.checkAt
            text = lastPing?.let {
                DateUtils.getRelativeTimeSpanString(
                    it.toEpochMilli(),
                    Instant.now().toEpochMilli(),
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()
            } ?: ""
            setTextColor(
                when {
                    status.tracked.isNotEmpty() -> getColorForAttr(com.google.android.material.R.attr.colorPrimary)
                    status.lastHit != null -> getColorForAttr(com.google.android.material.R.attr.colorSecondary)
                    else -> getColorForAttr(com.google.android.material.R.attr.colorError)
                }
            )
        }

        alertStatus.apply {
            text = when {
                status.tracked.isNotEmpty() -> getString(R.string.alerts_single_aircraft_spotted)

                status.lastHit != null -> {
                    val text = status.lastHit!!.checkAt.atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
                    getString(R.string.alerts_single_aircraft_last_spotted, text)
                }

                else -> getString(R.string.alerts_single_aircraft_not_spotted)
            }
            isGone = status.tracked.isNotEmpty()
        }

        thumbnail.apply {
            status.tracked.firstOrNull()?.let { load(it) }
            onViewImageListener = { item.onThumbnail(it) }
            isGone = status.tracked.size != 1
        }

        infoContainer.apply {
            isGone = status.tracked.size != 1
            status.tracked.firstOrNull()?.let { aircraft ->
                flightValue.text = aircraft.callsign
                squawkValue.text = aircraft.squawk
                typeValue.text = aircraft.airframe
            }
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