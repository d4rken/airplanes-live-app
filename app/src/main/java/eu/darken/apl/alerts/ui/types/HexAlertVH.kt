package eu.darken.apl.alerts.ui.types

import android.view.ViewGroup
import androidx.core.view.isGone
import eu.darken.apl.R
import eu.darken.apl.alerts.core.types.HexAlert
import eu.darken.apl.alerts.ui.AlertsListAdapter
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.databinding.AlertsListHexcodeItemBinding


class HexAlertVH(parent: ViewGroup) :
    AlertsListAdapter.BaseVH<HexAlertVH.Item, AlertsListHexcodeItemBinding>(
        R.layout.alerts_list_hexcode_item,
        parent
    ), BindableVH<HexAlertVH.Item, AlertsListHexcodeItemBinding> {

    override val viewBinding = lazy { AlertsListHexcodeItemBinding.bind(itemView) }

    override val onBindData: AlertsListHexcodeItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        val status = item.status

        title.text = status.hex

        lastTriggered.text = status.tracked.maxByOrNull { it.triggeredAt }?.toString() ?: ""

        alertStatus.text = getQuantityString(R.plurals.alerts_aircraft_spotted, status.tracked.size)

        infoContainer.isGone = status.tracked.size != 1
        if (status.tracked.size == 1) {
            val aircraft = status.tracked.first().aircraft
            flightValue.text = aircraft.callsign
            registrationValue.text = aircraft.registration
            squawkValue.text = aircraft.squawk
        }

        noteLabel.isGone = status.note.isBlank()
        noteValue.apply {
            isGone = status.note.isBlank()
            text = status.note
        }

        root.apply {
            setOnClickListener { item.onTap(item) }
        }
    }

    data class Item(
        val status: HexAlert.Status,
        val onTap: (Item) -> Unit,
    ) : AlertsListAdapter.Item {
        override val stableId: Long
            get() = status.id.hashCode().toLong()
    }
}