package eu.darken.apl.watchlist.core.db.types

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.watchlist.core.WatchId
import eu.darken.apl.watchlist.core.makeWatchIdForHex
import java.time.Instant

@Entity(
    tableName = "watch_aircraft",
)
data class AircraftWatchEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: WatchId = makeWatchIdForHex(),
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now(),
    @ColumnInfo(name = "user_note") val userNote: String = "",
    @ColumnInfo(name = "hex_code") val hexCode: AircraftHex,
    @ColumnInfo(name = "location_latitude") val latitude: Double? = null,
    @ColumnInfo(name = "location_longitude") val longitude: Double? = null,
    @ColumnInfo(name = "location_radius") val radius: Float? = null,
)