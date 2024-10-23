package eu.darken.apl.alerts.core.db.types

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import eu.darken.apl.alerts.core.AlertId
import eu.darken.apl.alerts.core.makeAlertIdForCallsign
import eu.darken.apl.main.core.aircraft.Callsign
import java.time.Instant

@Entity(
    tableName = "alerts_callsign",
)
data class CallsignAlertEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: AlertId = makeAlertIdForCallsign(),
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now(),
    @ColumnInfo(name = "user_note") val userNote: String = "",
    @ColumnInfo(name = "callsign") val callsign: Callsign,
    @ColumnInfo(name = "location_latitude") val latitude: Double? = null,
    @ColumnInfo(name = "location_longitude") val longitude: Double? = null,
    @ColumnInfo(name = "location_radius") val radius: Float? = null,
)