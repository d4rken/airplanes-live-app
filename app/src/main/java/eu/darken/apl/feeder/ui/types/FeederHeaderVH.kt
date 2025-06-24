package eu.darken.apl.feeder.ui.types

import android.view.ViewGroup
import eu.darken.apl.R
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.databinding.FeederListHeaderItemBinding
import eu.darken.apl.feeder.ui.FeederListAdapter

class FeederHeaderVH(parent: ViewGroup) :
    FeederListAdapter.BaseVH<FeederHeaderVH.Item, FeederListHeaderItemBinding>(
        R.layout.feeder_list_header_item,
        parent
    ), BindableVH<FeederHeaderVH.Item, FeederListHeaderItemBinding> {

    override val viewBinding = lazy { FeederListHeaderItemBinding.bind(itemView) }

    override val onBindData: FeederListHeaderItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        headerStatusText.text = if (item.hasOfflineFeeders) {
            context.getString(R.string.feeder_status_header_some_offline)
        } else {
            context.getString(R.string.feeder_status_header_all_online)
        }

        headerStatusText.setTextColor(
            if (item.hasOfflineFeeders) getColorForAttr(com.google.android.material.R.attr.colorError)
            else getColorForAttr(com.google.android.material.R.attr.colorPrimary)
        )
    }

    data class Item(
        val hasOfflineFeeders: Boolean
    ) : FeederListAdapter.Item {
        override val stableId: Long = Item::class.java.hashCode().toLong()
    }
}
