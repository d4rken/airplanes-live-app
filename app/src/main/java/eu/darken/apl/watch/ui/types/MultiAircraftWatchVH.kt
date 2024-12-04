package eu.darken.apl.watch.ui.types

import android.location.Location
import android.text.format.DateUtils
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import eu.darken.apl.R
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.common.lists.differ.update
import eu.darken.apl.common.lists.setupDefaults
import eu.darken.apl.common.planespotters.PlanespottersMeta
import eu.darken.apl.databinding.WatchlistListMultiItemBinding
import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.watch.core.types.AircraftWatch
import eu.darken.apl.watch.core.types.FlightWatch
import eu.darken.apl.watch.core.types.SquawkWatch
import eu.darken.apl.watch.core.types.Watch
import eu.darken.apl.watch.ui.WatchlistAdapter
import java.time.Instant


class MultiAircraftWatchVH(parent: ViewGroup) :
    WatchlistAdapter.BaseVH<MultiAircraftWatchVH.Item, WatchlistListMultiItemBinding>(
        R.layout.watchlist_list_multi_item,
        parent
    ), BindableVH<MultiAircraftWatchVH.Item, WatchlistListMultiItemBinding> {

    private val subAdapter by lazy { MultiAircraftAdapter() }

    override val viewBinding = lazy {
        WatchlistListMultiItemBinding.bind(itemView).apply {
            list.setupDefaults(subAdapter, verticalDividers = true)
        }
    }

    override val onBindData: WatchlistListMultiItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        val status = item.status

        title.text = when (status) {
            is AircraftWatch.Status -> throw IllegalArgumentException("VH does not support aircraft watch")
            is FlightWatch.Status -> throw IllegalArgumentException("VH does not support flight watch")
            is SquawkWatch.Status -> status.squawk.uppercase()
        }

        lastTriggered.apply {
            val lastPing = status.tracked.maxOfOrNull { it.seenAt } ?: status.lastHit?.checkAt
            text = lastPing?.let {
                DateUtils.getRelativeTimeSpanString(
                    it.toEpochMilli(),
                    Instant.now().toEpochMilli(),
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()
            } ?: getString(R.string.watch_list_spotted_never_label)
            setTextColor(
                when {
                    status.tracked.isNotEmpty() -> getColorForAttr(com.google.android.material.R.attr.colorPrimary)
                    status.lastHit != null -> getColorForAttr(com.google.android.material.R.attr.colorSecondary)
                    else -> getColorForAttr(com.google.android.material.R.attr.colorError)
                }
            )
        }

        run {
            val acItems = status.tracked
                .map { ac ->
                    val distance = if (item.ourLocation != null && ac.location != null) {
                        item.ourLocation.distanceTo(ac.location!!)
                    } else {
                        null
                    }
                    AircraftVH.Item(
                        ac = ac,
                        distanceInMeters = distance,
                        onTap = { item.onAircraftTap(it) },
                        onThumbnail = { item.onThumbnail(it) }
                    )
                }
                .sortedBy { it.distanceInMeters }
                .take(5)

            subAdapter.update(acItems)
            list.isGone = acItems.isEmpty()

            viewMoreAction.apply {
                isVisible = status.tracked.size > acItems.size
                val countText = getQuantityString(R.plurals.result_x_items, status.tracked.size)
                val actionText = getString(R.string.watch_list_show_all_action)
                text = "$countText ($actionText)"
                setOnClickListener { item.onShowMore() }
            }
        }

        noteBox.isGone = status.note.isBlank()
        noteValue.text = status.note

        root.apply {
            setOnClickListener { item.onTap(item) }
        }
    }

    data class Item(
        val status: Watch.Status,
        val ourLocation: Location?,
        val onShowMore: () -> Unit,
        val onTap: (Item) -> Unit,
        val onAircraftTap: (Aircraft) -> Unit,
        val onThumbnail: (PlanespottersMeta) -> Unit,
    ) : WatchlistAdapter.Item {
        override val stableId: Long
            get() = status.id.hashCode().toLong()
    }
}