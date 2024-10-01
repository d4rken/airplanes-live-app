package eu.darken.apl.search.ui.items

import android.view.ViewGroup
import eu.darken.apl.R
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.databinding.SearchListSummaryItemBinding
import eu.darken.apl.search.ui.SearchAdapter


class SummaryVH(parent: ViewGroup) :
    SearchAdapter.BaseVH<SummaryVH.Item, SearchListSummaryItemBinding>(
        R.layout.search_list_summary_item,
        parent
    ), BindableVH<SummaryVH.Item, SearchListSummaryItemBinding> {

    override val viewBinding = lazy { SearchListSummaryItemBinding.bind(itemView) }

    override val onBindData: SearchListSummaryItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, _ ->
        aircraftCount.text = getString(R.string.search_summary_x_aircraft, item.aircraftCount)
    }

    data class Item(
        val aircraftCount: Int,
    ) : SearchAdapter.Item {
        override val stableId: Long
            get() = Item::class.hashCode().toLong()
    }
}