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

        root.apply {
            setOnClickListener { item.onTap() }
            setOnLongClickListener {
                item.onLongPress()
                true
            }
        }

        alertRibbon.apply {
            isGone = item.alert == null
            setOnClickListener { item.onAlert(item.alert!!) }
        }
    }

    data class Item(
        val aircraft: Aircraft,
        val alert: AircraftAlert?,
        val distanceInMeter: Float?,
        val onTap: () -> Unit,
        val onLongPress: () -> Unit,
        val onThumbnail: (PlanespottersMeta) -> Unit,
        val onAlert: (AircraftAlert) -> Unit,
    ) : SearchAdapter.Item {
        override val stableId: Long
            get() = aircraft.hex.hashCode().toLong()
    }
}