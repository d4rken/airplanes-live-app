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
import eu.darken.apl.common.planespotters.coil.AircraftThumbnailQuery
import eu.darken.apl.common.planespotters.load
import eu.darken.apl.databinding.AlertsListSingleItemBinding
import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.messageTypeLabel
import java.time.Instant


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
        val aircraft = item.aircraft

        when (status) {
            is HexAlert.Status -> {
                title.text = aircraft?.registration ?: "?"
                title2.text = "| ${status.hex.uppercase()}"
                subtitle.text = getString(R.string.alerts_item_hexcode_subtitle)
                alertIcon.setImageResource(R.drawable.ic_hexagon_multiple_24)
            }

            is CallsignAlert.Status -> {
                title.text = status.callsign.uppercase()
                title2.text = "| #${aircraft?.hex?.uppercase() ?: "?"}"
                subtitle.text = getString(R.string.alerts_item_callsign_subtitle)
                alertIcon.setImageResource(R.drawable.ic_bullhorn_24)
            }

            is SquawkAlert.Status -> {
                throw IllegalArgumentException("Wrong VH for SQUAWK")
            }
        }

        lastTriggered.apply {
            setTextColor(
                when {
                    status.tracked.isNotEmpty() -> getColorForAttr(com.google.android.material.R.attr.colorPrimary)
                    status.lastHit != null -> getColorForAttr(com.google.android.material.R.attr.colorSecondary)
                    else -> getColorForAttr(com.google.android.material.R.attr.colorError)
                }
            )
            val lastPing = status.tracked.maxOfOrNull { it.seenAt } ?: status.lastHit?.checkAt
            text = lastPing?.let {
                DateUtils.getRelativeTimeSpanString(
                    it.toEpochMilli(),
                    Instant.now().toEpochMilli(),
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()
            } ?: getString(R.string.alerts_spotted_never_label)
        }

        extraInfo.text = aircraft?.messageTypeLabel ?: ""

        infoContainer.apply {
            thumbnail.apply {
                if (aircraft != null) {
                    load(aircraft)
                } else if (status is HexAlert.Status) {
                    load(AircraftThumbnailQuery(hex = status.hex))
                } else {
                    load(null)
                }
                onViewImageListener = { item.onThumbnail(it) }
            }

            firstValue.text = aircraft?.callsign ?: "?"
            secondValue.apply {
                text = aircraft?.squawk ?: "?"
                setTextColor(
                    when {
                        aircraft?.squawk?.startsWith("7") == true -> getColorForAttr(com.google.android.material.R.attr.colorError)
                        else -> getColorForAttr(com.google.android.material.R.attr.colorControlNormal)
                    }
                )
            }
            thirdValue.text = when {
                item.distanceInMeter != null -> "${(item.distanceInMeter / 1000).toInt()} km"

                else -> "?"
            }

            infoText.text = aircraft?.description ?: "?"
        }

        noteBox.isGone = status.note.isBlank()
        noteValue.text = status.note

        root.apply {
            setOnClickListener { item.onTap(item) }
        }
    }

    data class Item(
        val status: AircraftAlert.Status,
        val aircraft: Aircraft?,
        val distanceInMeter: Float?,
        val onTap: (Item) -> Unit,
        val onThumbnail: (PlanespottersMeta) -> Unit,
    ) : AlertsListAdapter.Item {
        override val stableId: Long
            get() = status.id.hashCode().toLong()
    }
}