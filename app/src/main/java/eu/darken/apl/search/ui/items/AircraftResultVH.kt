package eu.darken.apl.search.ui.items

import android.view.ViewGroup
import androidx.core.view.isGone
import eu.darken.apl.R
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.common.lists.selection.SelectableVH
import eu.darken.apl.common.planespotters.PlanespottersMeta
import eu.darken.apl.common.planespotters.load
import eu.darken.apl.databinding.SearchListAircraftItemBinding
import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.messageTypeLabel
import eu.darken.apl.search.ui.SearchAdapter
import eu.darken.apl.watch.core.types.Watch


class AircraftResultVH(parent: ViewGroup) :
    SearchAdapter.BaseVH<AircraftResultVH.Item, SearchListAircraftItemBinding>(
        R.layout.search_list_aircraft_item,
        parent
    ), BindableVH<AircraftResultVH.Item, SearchListAircraftItemBinding>, SelectableVH {

    override val viewBinding = lazy { SearchListAircraftItemBinding.bind(itemView) }

    private var lastItem: Item? = null
    override val itemSelectionKey: String?
        get() = lastItem?.itemSelectionKey

    override fun updatedSelectionState(selected: Boolean) {
        itemView.isActivated = selected
    }

    override val onBindData: SearchListAircraftItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, _ ->
        lastItem = item
        val aircraft = item.aircraft

        thumbnail.apply {
            load(aircraft)
            thumbnail.onViewImageListener = { item.onThumbnail(it) }
        }

        title.text = aircraft.registration ?: "?"
        title2.text = "| #${aircraft.hex}"
        subtitle.text = when {
            aircraft.description != null -> aircraft.description
            else -> "?"
        }
        extraInfo.text = aircraft.messageTypeLabel

        firstValue.text = aircraft.callsign?.takeIf { it.isNotBlank() } ?: "?"

        secondValue.apply {
            text = aircraft.squawk ?: "?"
            setTextColor(
                when {
                    aircraft.squawk?.startsWith("7") == true -> getColorForAttr(com.google.android.material.R.attr.colorError)
                    else -> getColorForAttr(com.google.android.material.R.attr.colorControlNormal)
                }
            )
        }

        thirdValue.text = when {
            item.distanceInMeter != null -> "${(item.distanceInMeter / 1000).toInt()} km"

            else -> "?"
        }

        root.setOnClickListener { item.onTap() }

        watchRibbon.apply {
            isGone = item.watch == null
            setOnClickListener { item.onWatch(item.watch!!) }
        }
    }

    data class Item(
        val aircraft: Aircraft,
        val watch: Watch?,
        val distanceInMeter: Float?,
        val onTap: () -> Unit,
        val onThumbnail: (PlanespottersMeta) -> Unit,
        val onWatch: (Watch) -> Unit,
    ) : SearchAdapter.Item {
        override val stableId: Long
            get() = aircraft.hex.hashCode().toLong()
        override val itemSelectionKey: String
            get() = aircraft.hex
    }
}