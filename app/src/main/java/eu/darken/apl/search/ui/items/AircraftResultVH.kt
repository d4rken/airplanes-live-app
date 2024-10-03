package eu.darken.apl.search.ui.items

import android.view.ViewGroup
import androidx.core.view.isGone
import eu.darken.apl.R
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.common.planespotters.PlanespottersMeta
import eu.darken.apl.common.planespotters.load
import eu.darken.apl.databinding.SearchListAircraftItemBinding
import eu.darken.apl.main.core.aircraft.Aircraft
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

        title.text = "${aircraft.label} (${aircraft.hex.uppercase()})"
        subtitle.apply {
            text = aircraft.description
            aircraft.operator?.let { append(" from $it") }
        }
        distance.apply {
            text = item.distanceInMeter?.let {
                getString(R.string.general_xdistance_away_label, "${(it / 1000).toInt()}km")
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
    }

    data class Item(
        val aircraft: Aircraft,
        val distanceInMeter: Float?,
        val onTap: () -> Unit,
        val onLongPress: () -> Unit,
        val onThumbnail: (PlanespottersMeta) -> Unit,
    ) : SearchAdapter.Item {
        override val stableId: Long
            get() = aircraft.id.hashCode().toLong()
    }
}