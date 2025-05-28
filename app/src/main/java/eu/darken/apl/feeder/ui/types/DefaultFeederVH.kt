package eu.darken.apl.feeder.ui.types

import android.text.format.DateUtils
import android.view.ViewGroup
import androidx.core.view.isGone
import eu.darken.apl.R
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.common.lists.selection.SelectableVH
import eu.darken.apl.databinding.FeederListDefaultItemBinding
import eu.darken.apl.feeder.core.Feeder
import eu.darken.apl.feeder.ui.FeederListAdapter
import java.time.Instant


class DefaultFeederVH(parent: ViewGroup) :
    FeederListAdapter.BaseVH<DefaultFeederVH.Item, FeederListDefaultItemBinding>(
        R.layout.feeder_list_default_item,
        parent
    ), BindableVH<DefaultFeederVH.Item, FeederListDefaultItemBinding>, SelectableVH {

    override val viewBinding = lazy { FeederListDefaultItemBinding.bind(itemView) }

    private var lastItem: Item? = null
    override val itemSelectionKey: String?
        get() = lastItem?.itemSelectionKey

    override fun updatedSelectionState(selected: Boolean) {
        itemView.isActivated = selected
    }

    override val onBindData: FeederListDefaultItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        lastItem = item
        val feeder = item.feeder
        receiverName.apply {
            text = feeder.label
            setTextColor(
                if (feeder.isOffline) getColorForAttr(com.google.android.material.R.attr.colorError)
                else getColorForAttr(com.google.android.material.R.attr.colorOnBackground)
            )
        }

        lastSeen.text = feeder.lastSeen?.let {
            DateUtils.getRelativeTimeSpanString(
                it.toEpochMilli(),
                Instant.now().toEpochMilli(),
                DateUtils.MINUTE_IN_MILLIS
            )
        } ?: "?"

        beastMsgRate.text = feeder.beastStats?.messageRate?.let { "$it MSG/s" } ?: "? MSG/s unavailable"
        beastBandwidthRate.text = feeder.beastStats?.bandwidth?.let { "$it KBit/s" } ?: "Bandwith unavailable"

        mlatMsgRate.text = feeder.mlatStats?.messageRate?.let { "$it MSG/s" } ?: "MSG/s unavailable"
        mlatOutlierPercent.apply {
            text = feeder.mlatStats?.outlierPercent?.let { "$it% outliers" } ?: "Outliers unavailable"
            feeder.mlatStats?.peerCount?.let {
                append(" ($it peers)")
            }
        }

        monitorIcon.isGone = feeder.config.offlineCheckTimeout == null
        monitorIcon.setImageResource(
            if (feeder.isOffline) R.drawable.ic_fire_alert_24 else R.drawable.ic_alarm_bell_24
        )

        root.setOnClickListener { item.onTap(item) }
    }

    data class Item(
        val feeder: Feeder,
        val onTap: (Item) -> Unit,
    ) : FeederListAdapter.Item {
        override val stableId: Long
            get() = feeder.id.hashCode().toLong()
        override val itemSelectionKey: String
            get() = feeder.id
    }
}