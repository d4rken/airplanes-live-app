package eu.darken.apl.alerts.core.config.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import eu.darken.apl.alerts.core.AlertId
import eu.darken.apl.alerts.core.makeAlertIdForHex
import eu.darken.apl.main.core.aircraft.AircraftHex
import java.time.Instant

@Entity(
    tableName = "alerts_hex",
)
data class HexAlertEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: AlertId = makeAlertIdForHex(),
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now(),
    @ColumnInfo(name = "user_note") val userNote: String = "",
    @ColumnInfo(name = "hex_code") val hexCode: AircraftHex,
    @ColumnInfo(name = "location_latitude") val latitude: Double? = null,
    @ColumnInfo(name = "location_longitude") val longitude: Double? = null,
    @ColumnInfo(name = "location_radius") val radius: Float? = null,
)