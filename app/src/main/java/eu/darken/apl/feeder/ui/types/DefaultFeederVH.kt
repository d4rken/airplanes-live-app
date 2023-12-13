package eu.darken.apl.feeder.ui.types

import android.text.format.DateUtils
import android.view.ViewGroup
import androidx.core.view.isGone
import eu.darken.apl.R
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.databinding.FeederListDefaultItemBinding
import eu.darken.apl.feeder.core.Feeder
import eu.darken.apl.feeder.ui.FeederListAdapter
import java.time.Instant


class DefaultFeederVH(parent: ViewGroup) :
    FeederListAdapter.BaseVH<DefaultFeederVH.Item, FeederListDefaultItemBinding>(
        R.layout.feeder_list_default_item,
        parent
    ), BindableVH<DefaultFeederVH.Item, FeederListDefaultItemBinding> {

    override val viewBinding = lazy { FeederListDefaultItemBinding.bind(itemView) }

    override val onBindData: FeederListDefaultItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
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
        mlatOutlierPercent.text = feeder.mlatStats?.outlierPercent?.let { "$it% outliers" } ?: "Outliers unavailable"

        monitorIcon.isGone = feeder.config.offlineCheckTimeout == null
        monitorIcon.setImageResource(
            if (feeder.isOffline) R.drawable.ic_fire_alert_24 else R.drawable.ic_alarm_bell_24
        )

        root.apply {
            setOnClickListener { item.onTap(item) }
            setOnLongClickListener {
                item.onLongPress(item)
                true
            }
        }
    }

    data class Item(
        val feeder: Feeder,
        val onTap: (Item) -> Unit,
        val onLongPress: (Item) -> Unit,
    ) : FeederListAdapter.Item {
        override val stableId: Long
            get() = feeder.id.hashCode().toLong()
    }
}