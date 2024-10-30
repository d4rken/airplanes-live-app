package eu.darken.apl.watchlist.core.db.types

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import eu.darken.apl.main.core.aircraft.SquawkCode
import eu.darken.apl.watchlist.core.WatchId
import eu.darken.apl.watchlist.core.makeWatchIdForSquawk
import java.time.Instant

@Entity(
    tableName = "watch_squawk",
)
data class SquawkWatchEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: WatchId = makeWatchIdForSquawk(),
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now(),
    @ColumnInfo(name = "user_note") val userNote: String = "",
    @ColumnInfo(name = "code") val code: SquawkCode,
    @ColumnInfo(name = "location_latitude") val latitude: Double? = null,
    @ColumnInfo(name = "location_longitude") val longitude: Double? = null,
    @ColumnInfo(name = "location_radius") val radius: Float? = null,
)