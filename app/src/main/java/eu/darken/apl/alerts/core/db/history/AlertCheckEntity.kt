package eu.darken.apl.alerts.core.db.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import eu.darken.apl.alerts.core.AlertId
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "alert_checks",
)
data class AlertCheckEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "checked_at") val checkedAt: Instant = Instant.now(),
    @ColumnInfo(name = "alert_id") val alertId: AlertId,
    @ColumnInfo(name = "aircraft_count") val aircraftcount: Int,
)