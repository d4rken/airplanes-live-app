package eu.darken.apl.watchlist.ui.types

import android.view.ViewGroup
import eu.darken.apl.R
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.common.planespotters.PlanespottersMeta
import eu.darken.apl.common.planespotters.load
import eu.darken.apl.databinding.WatchlistListMultiItemItemBinding
import eu.darken.apl.main.core.aircraft.Aircraft


class AircraftVH(parent: ViewGroup) :
    MultiAircraftAdapter.BaseVH<AircraftVH.Item, WatchlistListMultiItemItemBinding>(
        R.layout.watchlist_list_multi_item_item,
        parent
    ), BindableVH<AircraftVH.Item, WatchlistListMultiItemItemBinding> {

    override val viewBinding = lazy { WatchlistListMultiItemItemBinding.bind(itemView) }

    override val onBindData: WatchlistListMultiItemItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        val ac = item.ac

        thumbnail.apply {
            load(ac)
            onViewImageListener = { item.onThumbnail(it) }
        }

        infoContainer.apply {
            flightValue.text = ac.callsign
            registrationValue.text = ac.registration
            typeValue.text = ac.airframe
        }

        root.apply {
            setOnClickListener { item.onTap(ac) }
        }
    }

    data class Item(
        val ac: Aircraft,
        val onTap: (Aircraft) -> Unit,
        val onThumbnail: (PlanespottersMeta) -> Unit,
    ) : MultiAircraftAdapter.Item {
        override val stableId: Long
            get() = ac.hex.hashCode().toLong()
    }
}