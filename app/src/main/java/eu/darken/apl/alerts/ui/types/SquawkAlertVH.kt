package eu.darken.apl.alerts.ui.types

import android.view.ViewGroup
import androidx.core.view.isGone
import eu.darken.apl.R
import eu.darken.apl.alerts.core.types.SquawkAlert
import eu.darken.apl.alerts.ui.AlertsListAdapter
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.databinding.AlertsListSquawkItemBinding


class SquawkAlertVH(parent: ViewGroup) :
    AlertsListAdapter.BaseVH<SquawkAlertVH.Item, AlertsListSquawkItemBinding>(
        R.layout.alerts_list_squawk_item,
        parent
    ), BindableVH<SquawkAlertVH.Item, AlertsListSquawkItemBinding> {

    override val viewBinding = lazy { AlertsListSquawkItemBinding.bind(itemView) }

    override val onBindData: AlertsListSquawkItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        val status = item.status

        title.text = status.squawk

        lastTriggered.text = status.tracked.maxByOrNull { it.triggeredAt }?.toString() ?: ""

        alertStatus.text = getQuantityString(R.plurals.alerts_aircraft_spotted, status.tracked.size)

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
        val status: SquawkAlert.Status,
        val onTap: (Item) -> Unit,
    ) : AlertsListAdapter.Item {
        override val stableId: Long
            get() = status.id.hashCode().toLong()
    }
}