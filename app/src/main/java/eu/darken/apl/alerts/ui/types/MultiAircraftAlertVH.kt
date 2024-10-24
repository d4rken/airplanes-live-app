package eu.darken.apl.alerts.ui.types

import android.text.format.DateUtils
import android.view.ViewGroup
import androidx.core.view.isGone
import eu.darken.apl.R
import eu.darken.apl.alerts.core.types.AircraftAlert
import eu.darken.apl.alerts.core.types.CallsignAlert
import eu.darken.apl.alerts.core.types.HexAlert
import eu.darken.apl.alerts.core.types.SquawkAlert
import eu.darken.apl.alerts.ui.AlertsListAdapter
import eu.darken.apl.common.getQuantityString
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.common.lists.differ.update
import eu.darken.apl.common.lists.setupDefaults
import eu.darken.apl.common.planespotters.PlanespottersMeta
import eu.darken.apl.databinding.AlertsListMultiItemBinding
import eu.darken.apl.main.core.aircraft.Aircraft
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class MultiAircraftAlertVH(parent: ViewGroup) :
    AlertsListAdapter.BaseVH<MultiAircraftAlertVH.Item, AlertsListMultiItemBinding>(
        R.layout.alerts_list_multi_item,
        parent
    ), BindableVH<MultiAircraftAlertVH.Item, AlertsListMultiItemBinding> {

    private val subAdapter by lazy { MultiAircraftAdapter() }

    override val viewBinding = lazy {
        AlertsListMultiItemBinding.bind(itemView).apply {
            list.setupDefaults(subAdapter, dividers = true)
        }
    }

    override val onBindData: AlertsListMultiItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        val status = item.status

        title.text = when (status) {
            is HexAlert.Status -> throw IllegalArgumentException("VH does not support hex alerts")
            is CallsignAlert.Status -> throw IllegalArgumentException("VH does not support callsign alerts")
            is SquawkAlert.Status -> status.squawk.uppercase()
        }

        lastTriggered.apply {
            val lastPing = status.tracked.maxOfOrNull { it.seenAt } ?: status.lastHit?.checkAt
            text = lastPing?.let {
                DateUtils.getRelativeTimeSpanString(
                    it.toEpochMilli(),
                    Instant.now().toEpochMilli(),
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()
            } ?: ""
            setTextColor(
                when {
                    status.tracked.isNotEmpty() -> getColorForAttr(com.google.android.material.R.attr.colorPrimary)
                    status.lastHit != null -> getColorForAttr(com.google.android.material.R.attr.colorSecondary)
                    else -> getColorForAttr(com.google.android.material.R.attr.colorError)
                }
            )
        }

        alertStatus.apply {
            when {
                status.tracked.isNotEmpty() -> {
                    text = ""
                    isGone = true
                }

                status.lastHit != null -> {
                    val dateText = status.lastHit!!.checkAt.atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
                    text = context.getQuantityString(
                        R.plurals.alerts_multi_aircrafts_last_spotted,
                        status.lastHit!!.aircraftCount,
                        status.lastHit!!.aircraftCount,
                        dateText
                    )
                    isGone = false
                }

                else -> {
                    text = getString(R.string.alerts_multi_aircraft_not_spotted)
                    isGone = false
                }
            }
            setTextColor(
                when {
                    status.tracked.isNotEmpty() -> getColorForAttr(com.google.android.material.R.attr.colorPrimary)
                    status.lastCheck != null -> getColorForAttr(com.google.android.material.R.attr.colorSecondary)
                    else -> getColorForAttr(com.google.android.material.R.attr.colorError)
                }
            )
        }

        run {
            val acItems = status.tracked.map { ac ->
                AircraftVH.Item(
                    ac = ac,
                    onTap = { item.onAircraftTap(it) },
                    onThumbnail = { item.onThumbnail(it) }
                )
            }
            subAdapter.update(acItems)
            list.isGone = acItems.isEmpty()
        }

        noteBox.isGone = status.note.isBlank()
        noteValue.text = status.note

        root.apply {
            setOnClickListener { item.onTap(item) }
        }
    }

    data class Item(
        val status: AircraftAlert.Status,
        val onTap: (Item) -> Unit,
        val onAircraftTap: (Aircraft) -> Unit,
        val onThumbnail: (PlanespottersMeta) -> Unit,
    ) : AlertsListAdapter.Item {
        override val stableId: Long
            get() = status.id.hashCode().toLong()
    }
}