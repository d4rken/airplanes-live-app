package eu.darken.apl.watch.ui.types

import android.view.ViewGroup
import eu.darken.apl.R
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.common.planespotters.PlanespottersMeta
import eu.darken.apl.common.planespotters.load
import eu.darken.apl.databinding.WatchListMultiItemItemBinding
import eu.darken.apl.main.core.aircraft.Aircraft


class AircraftVH(parent: ViewGroup) :
    MultiAircraftAdapter.BaseVH<AircraftVH.Item, WatchListMultiItemItemBinding>(
        R.layout.watch_list_multi_item_item,
        parent
    ), BindableVH<AircraftVH.Item, WatchListMultiItemItemBinding> {

    override val viewBinding = lazy { WatchListMultiItemItemBinding.bind(itemView) }

    override val onBindData: WatchListMultiItemItemBinding.(
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
        val distanceInMeters: Float?,
        val onTap: (Aircraft) -> Unit,
        val onThumbnail: (PlanespottersMeta) -> Unit,
    ) : MultiAircraftAdapter.Item {
        override val stableId: Long
            get() = ac.hex.hashCode().toLong()
    }
}