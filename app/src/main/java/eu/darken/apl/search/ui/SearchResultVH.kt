package eu.darken.apl.search.ui

import android.view.ViewGroup
import eu.darken.apl.R
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.databinding.SearchListResultItemBinding
import eu.darken.apl.main.core.aircraft.Aircraft


class SearchResultVH(parent: ViewGroup) :
    SearchAdapter.BaseVH<SearchResultVH.Item, SearchListResultItemBinding>(
        R.layout.search_list_result_item,
        parent
    ), BindableVH<SearchResultVH.Item, SearchListResultItemBinding> {

    override val viewBinding = lazy { SearchListResultItemBinding.bind(itemView) }

    override val onBindData: SearchListResultItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, _ ->
        val aircraft = item.aircraft

        title.text = "${aircraft.label} (${aircraft.hex.uppercase()})"
        subtitle.apply {
            text = aircraft.description
            aircraft.operator?.let { append(" from $it") }
        }
        distance.text = "${(item.distanceInMeter / 1000).toInt()} km from you"

        flightValue.text = aircraft.callsign ?: "?"
        squawkValue.text = aircraft.squawk ?: "?"

        root.apply {
            setOnClickListener { item.onTap(item) }
            setOnLongClickListener {
                item.onLongPress(item)
                true
            }
        }
    }

    data class Item(
        val aircraft: Aircraft,
        val distanceInMeter: Long,
        val onTap: (Item) -> Unit,
        val onLongPress: (Item) -> Unit,
    ) : SearchAdapter.Item {
        override val stableId: Long
            get() = aircraft.id.hashCode().toLong()
    }
}