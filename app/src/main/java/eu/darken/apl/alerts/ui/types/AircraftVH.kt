package eu.darken.apl.alerts.ui.types

import android.view.ViewGroup
import eu.darken.apl.R
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.common.planespotters.PlanespottersMeta
import eu.darken.apl.common.planespotters.load
import eu.darken.apl.databinding.AlertsListMultiItemItemBinding
import eu.darken.apl.main.core.aircraft.Aircraft


class AircraftVH(parent: ViewGroup) :
    MultiAircraftAdapter.BaseVH<AircraftVH.Item, AlertsListMultiItemItemBinding>(
        R.layout.alerts_list_multi_item_item,
        parent
    ), BindableVH<AircraftVH.Item, AlertsListMultiItemItemBinding> {

    override val viewBinding = lazy { AlertsListMultiItemItemBinding.bind(itemView) }

    override val onBindData: AlertsListMultiItemItemBinding.(
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