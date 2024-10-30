package eu.darken.apl.watchlist.ui.types

import android.text.format.DateUtils
import android.view.ViewGroup
import androidx.core.view.isGone
import eu.darken.apl.R
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.common.lists.differ.update
import eu.darken.apl.common.lists.setupDefaults
import eu.darken.apl.common.planespotters.PlanespottersMeta
import eu.darken.apl.databinding.WatchlistListMultiItemBinding
import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.watchlist.core.types.AircraftWatch
import eu.darken.apl.watchlist.core.types.FlightWatch
import eu.darken.apl.watchlist.core.types.SquawkWatch
import eu.darken.apl.watchlist.core.types.Watch
import eu.darken.apl.watchlist.ui.WatchlistAdapter
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
            } ?: getString(R.string.watchlist_spotted_never_label)
            setTextColor(
                when {
                    status.tracked.isNotEmpty() -> getColorForAttr(com.google.android.material.R.attr.colorPrimary)
                    status.lastHit != null -> getColorForAttr(com.google.android.material.R.attr.colorSecondary)
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
        val status: Watch.Status,
        val onTap: (Item) -> Unit,
        val onAircraftTap: (Aircraft) -> Unit,
        val onThumbnail: (PlanespottersMeta) -> Unit,
    ) : WatchlistAdapter.Item {
        override val stableId: Long
            get() = status.id.hashCode().toLong()
    }
}