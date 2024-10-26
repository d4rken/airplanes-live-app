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
        val aircraft = status.tracked.singleOrNull()

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

        distance.apply {
            text = when {
                aircraft == null -> ""
                item.distanceInMeter != null -> {
                    val distText = getString(
                        R.string.general_xdistance_away_label,
                        "${(item.distanceInMeter / 1000).toInt()}km"
                    )
                    "$distText (${aircraft.messageTypeLabel})"
                }

                else -> aircraft.messageTypeLabel
            }
        }

        infoContainer.apply {
            when {
                aircraft != null -> {
                    firstValue.text = aircraft.callsign ?: "?"
                    secondValue.text = aircraft.registration ?: "?"
                    thirdValue.text = aircraft.airframe ?: "?"
                    fourthValue.text = aircraft.squawk ?: "?"
                    thumbnail.load(aircraft)
                    isGone = false
                }

                status is HexAlert.Status -> {
                    firstValue.text = "?"
                    secondValue.text = "?"
                    thirdValue.text = "?"
                    fourthValue.text = "?"
                    thumbnail.load(AircraftThumbnailQuery(hex = status.hex))
                    isGone = false
                }

                else -> {
                    isGone = true
                }
            }

            thumbnail.onViewImageListener = { item.onThumbnail(it) }
        }

        noteBox.isGone = status.note.isBlank()
        noteValue.text = status.note

        root.apply {
            setOnClickListener { item.onTap(item) }
        }
    }

    data class Item(
        val status: AircraftAlert.Status,
        val distanceInMeter: Float?,
        val onTap: (Item) -> Unit,
        val onThumbnail: (PlanespottersMeta) -> Unit,
    ) : AlertsListAdapter.Item {
        override val stableId: Long
            get() = status.id.hashCode().toLong()
    }
}