package eu.darken.apl.search.ui.items

import android.view.ViewGroup
import eu.darken.apl.R
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.databinding.SearchListProgressItemBinding
import eu.darken.apl.search.ui.SearchAdapter
import eu.darken.apl.search.ui.SearchViewModel


class SearchingAircraftVH(parent: ViewGroup) :
    SearchAdapter.BaseVH<SearchingAircraftVH.Item, SearchListProgressItemBinding>(
        R.layout.search_list_progress_item,
        parent
    ), BindableVH<SearchingAircraftVH.Item, SearchListProgressItemBinding> {

    override val viewBinding = lazy { SearchListProgressItemBinding.bind(itemView) }

    override val onBindData: SearchListProgressItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, _ ->
        body.text = getString(R.string.search_progress_body, item.aircraftCount)
    }

    data class Item(
        val query: SearchViewModel.Input,
        val aircraftCount: Int,
    ) : SearchAdapter.Item {
        override val stableId: Long
            get() = query.hashCode().toLong()
    }
}