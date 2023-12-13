package eu.darken.apl.alerts.ui.types

import android.view.ViewGroup
import androidx.core.view.isVisible
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
        val alert = item.alert

        title.text = alert.label
        subtitle.text = alert.hex

        notfoundContainer.isVisible = alert.infos.isEmpty()
        aircraftContainer.isVisible = alert.infos.isNotEmpty()

        if (alert.infos.isEmpty()) {
            alertStatus.text = getString(R.string.alerts_hexcode_notfound_message)
        } else {
            val aircraft = alert.infos.first()
            flightValue.text = aircraft.flight
            registrationValue.text = aircraft.registration
            squawkValue.text = aircraft.squawk
            descriptionValue.text = "${aircraft.description} @ ${aircraft.altitude} ft"
        }

        root.apply {
            setOnClickListener { item.onTap(item) }
            setOnLongClickListener {
                item.onLongPress(item)
                true
            }
        }
    }

    data class Item(
        val alert: HexAlert,
        val onTap: (Item) -> Unit,
        val onLongPress: (Item) -> Unit,
    ) : AlertsListAdapter.Item {
        override val stableId: Long
            get() = alert.id.hashCode().toLong()
    }
}