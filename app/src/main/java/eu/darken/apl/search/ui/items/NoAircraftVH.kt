package eu.darken.apl.search.ui.items

import android.view.ViewGroup
import eu.darken.apl.R
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.databinding.SearchListEmptyItemBinding
import eu.darken.apl.search.ui.SearchAdapter
import eu.darken.apl.search.ui.SearchViewModel


class NoAircraftVH(parent: ViewGroup) :
    SearchAdapter.BaseVH<NoAircraftVH.Item, SearchListEmptyItemBinding>(
        R.layout.search_list_empty_item,
        parent
    ), BindableVH<NoAircraftVH.Item, SearchListEmptyItemBinding> {

    override val viewBinding = lazy { SearchListEmptyItemBinding.bind(itemView) }

    override val onBindData: SearchListEmptyItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, _ ->
        startFeedingAction.setOnClickListener { item.onStartFeeding() }
    }

    data class Item(
        val query: SearchViewModel.Input,
        val onStartFeeding: () -> Unit,
    ) : SearchAdapter.Item {
        override val stableId: Long
            get() = Item::class.hashCode().toLong()
    }
}