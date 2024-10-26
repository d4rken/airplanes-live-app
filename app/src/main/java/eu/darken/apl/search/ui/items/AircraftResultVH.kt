package eu.darken.apl.search.ui.items

import android.view.ViewGroup
import androidx.core.view.isGone
import eu.darken.apl.R
import eu.darken.apl.alerts.core.types.AircraftAlert
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.common.planespotters.PlanespottersMeta
import eu.darken.apl.common.planespotters.load
import eu.darken.apl.databinding.SearchListAircraftItemBinding
import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.messageTypeLabel
import eu.darken.apl.search.ui.SearchAdapter


class AircraftResultVH(parent: ViewGroup) :
    SearchAdapter.BaseVH<AircraftResultVH.Item, SearchListAircraftItemBinding>(
        R.layout.search_list_aircraft_item,
        parent
    ), BindableVH<AircraftResultVH.Item, SearchListAircraftItemBinding> {

    override val viewBinding = lazy { SearchListAircraftItemBinding.bind(itemView) }

    override val onBindData: SearchListAircraftItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, _ ->
        val aircraft = item.aircraft

        title.text = "${aircraft.registration} (#${aircraft.hex.uppercase()})"
        subtitle.apply {
            text = aircraft.description
            aircraft.operator?.let { append(" from $it") }
        }
        distance.apply {
            if (item.distanceInMeter != null) {
                val distText = getString(
                    R.string.general_xdistance_away_label,
                    "${(item.distanceInMeter / 1000).toInt()}km"
                )
                text = "$distText (${aircraft.messageTypeLabel})"
            } else {
                text = aircraft.messageTypeLabel
            }

            isGone = item.distanceInMeter == null
        }

        flightValue.text = aircraft.callsign ?: "?"
        squawkValue.text = aircraft.squawk ?: "?"

        root.apply {
            setOnClickListener { item.onTap() }
            setOnLongClickListener {
                item.onLongPress()
                true
            }
        }

        thumbnail.apply {
            load(aircraft)
            onViewImageListener = { item.onThumbnail(it) }
        }

        alertsInfo.apply {
            isGone = item.alerts.isEmpty()
            text = getQuantityString(R.plurals.search_aircraft_matching_alerts_desc, item.alerts.size)
        }
    }

    data class Item(
        val aircraft: Aircraft,
        val alerts: Collection<AircraftAlert>,
        val distanceInMeter: Float?,
        val onTap: () -> Unit,
        val onLongPress: () -> Unit,
        val onThumbnail: (PlanespottersMeta) -> Unit,
    ) : SearchAdapter.Item {
        override val stableId: Long
            get() = aircraft.hex.hashCode().toLong()
    }
}