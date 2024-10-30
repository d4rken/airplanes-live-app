package eu.darken.apl.watchlist.core.db.types

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import eu.darken.apl.main.core.aircraft.Callsign
import eu.darken.apl.watchlist.core.WatchId
import eu.darken.apl.watchlist.core.makeWatchIdForCallsign
import java.time.Instant

@Entity(
    tableName = "watch_flight",
)
data class FlightWatchEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: WatchId = makeWatchIdForCallsign(),
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now(),
    @ColumnInfo(name = "user_note") val userNote: String = "",
    @ColumnInfo(name = "callsign") val callsign: Callsign,
    @ColumnInfo(name = "location_latitude") val latitude: Double? = null,
    @ColumnInfo(name = "location_longitude") val longitude: Double? = null,
    @ColumnInfo(name = "location_radius") val radius: Float? = null,
)