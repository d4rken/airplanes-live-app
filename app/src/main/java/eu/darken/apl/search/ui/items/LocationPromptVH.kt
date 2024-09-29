package eu.darken.apl.search.ui.items

import android.view.ViewGroup
import eu.darken.apl.R
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.common.location.LocationManager2
import eu.darken.apl.databinding.SearchListLocationItemBinding
import eu.darken.apl.search.ui.SearchAdapter


class LocationPromptVH(parent: ViewGroup) :
    SearchAdapter.BaseVH<LocationPromptVH.Item, SearchListLocationItemBinding>(
        R.layout.search_list_location_item,
        parent
    ), BindableVH<LocationPromptVH.Item, SearchListLocationItemBinding> {

    override val viewBinding = lazy { SearchListLocationItemBinding.bind(itemView) }

    override val onBindData: SearchListLocationItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, _ ->
        grantAction.setOnClickListener { item.onGrant() }
        dismissAction.setOnClickListener { item.onDismiss() }
    }

    data class Item(
        val locationState: LocationManager2.State,
        val onDismiss: () -> Unit,
        val onGrant: () -> Unit,
    ) : SearchAdapter.Item {
        override val stableId: Long
            get() = locationState.hashCode().toLong()
    }
}