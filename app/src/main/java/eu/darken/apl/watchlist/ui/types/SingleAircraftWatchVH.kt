package eu.darken.apl.watchlist.ui.types

import android.location.Location
import android.text.format.DateUtils
import android.view.ViewGroup
import androidx.core.view.isGone
import eu.darken.apl.R
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.common.planespotters.PlanespottersMeta
import eu.darken.apl.common.planespotters.coil.AircraftThumbnailQuery
import eu.darken.apl.common.planespotters.load
import eu.darken.apl.databinding.WatchlistListSingleItemBinding
import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.messageTypeLabel
import eu.darken.apl.watchlist.core.types.AircraftWatch
import eu.darken.apl.watchlist.core.types.FlightWatch
import eu.darken.apl.watchlist.core.types.SquawkWatch
import eu.darken.apl.watchlist.core.types.Watch
import eu.darken.apl.watchlist.ui.WatchlistAdapter
import java.time.Instant


class SingleAircraftWatchVH(parent: ViewGroup) :
    WatchlistAdapter.BaseVH<SingleAircraftWatchVH.Item, WatchlistListSingleItemBinding>(
        R.layout.watchlist_list_single_item,
        parent
    ), BindableVH<SingleAircraftWatchVH.Item, WatchlistListSingleItemBinding> {

    override val viewBinding = lazy { WatchlistListSingleItemBinding.bind(itemView) }

    override val onBindData: WatchlistListSingleItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        val status = item.status
        val aircraft = item.aircraft

        when (status) {
            is AircraftWatch.Status -> {
                title.text = aircraft?.registration ?: "?"
                title2.text = "| ${status.hex.uppercase()}"
                subtitle.text = getString(R.string.watchlist_item_aircraft_subtitle)
                alertIcon.setImageResource(R.drawable.ic_hexagon_multiple_24)
            }

            is FlightWatch.Status -> {
                title.text = status.callsign.uppercase()
                title2.text = "| #${aircraft?.hex?.uppercase() ?: "?"}"
                subtitle.text = getString(R.string.watchlist_item_flight_subtitle)
                alertIcon.setImageResource(R.drawable.ic_bullhorn_24)
            }

            is SquawkWatch.Status -> {
                throw IllegalArgumentException("Wrong VH for SQUAWK")
            }
        }

        lastTriggered.apply {
            setTextColor(
                when {
                    status.tracked.isNotEmpty() -> getColorForAttr(com.google.android.material.R.attr.colorPrimary)
                    status.lastHit != null -> getColorForAttr(com.google.android.material.R.attr.colorSecondary)
                    else -> getColorForAttr(com.google.android.material.R.attr.colorError)
                }
            )
            val lastPing = status.tracked.maxOfOrNull { it.seenAt } ?: status.lastHit?.checkAt
            text = lastPing?.let {
                DateUtils.getRelativeTimeSpanString(
                    it.toEpochMilli(),
                    Instant.now().toEpochMilli(),
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()
            } ?: getString(R.string.watchlist_spotted_never_label)
        }

        extraInfo.text = aircraft?.messageTypeLabel ?: ""

        infoContainer.apply {
            thumbnail.apply {
                if (aircraft != null) {
                    load(aircraft)
                } else if (status is AircraftWatch.Status) {
                    load(AircraftThumbnailQuery(hex = status.hex))
                } else {
                    load(null)
                }
                onViewImageListener = { item.onThumbnail(it) }
            }

            firstValue.text = aircraft?.callsign ?: "?"
            secondValue.apply {
                text = aircraft?.squawk ?: "?"
                setTextColor(
                    when {
                        aircraft?.squawk?.startsWith("7") == true -> getColorForAttr(com.google.android.material.R.attr.colorError)
                        else -> getColorForAttr(com.google.android.material.R.attr.colorControlNormal)
                    }
                )
            }
            thirdValue.text = when {
                item.ourLocation != null && aircraft?.location != null -> {
                    val distanceInMeter = item.ourLocation.distanceTo(aircraft.location!!)
                    "${(distanceInMeter / 1000).toInt()} km"
                }

                else -> "?"
            }

            infoText.text = aircraft?.description ?: "?"
        }

        noteBox.isGone = status.note.isBlank()
        noteValue.text = status.note

        root.apply {
            setOnClickListener { item.onTap(item) }
        }
    }

    data class Item(
        val status: Watch.Status,
        val aircraft: Aircraft?,
        val ourLocation: Location?,
        val onTap: (Item) -> Unit,
        val onThumbnail: (PlanespottersMeta) -> Unit,
    ) : WatchlistAdapter.Item {
        override val stableId: Long
            get() = status.id.hashCode().toLong()
    }
}