package eu.darken.apl.alerts.core.db.types

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import eu.darken.apl.alerts.core.AlertId
import eu.darken.apl.alerts.core.db.AlertsDatabase
import eu.darken.apl.common.debug.logging.log
import kotlinx.coroutines.flow.Flow

@Dao
interface HexAlertsDao {
    @Query("SELECT * FROM alerts_hex WHERE id = :alertId")
    suspend fun get(alertId: AlertId): HexAlertEntity?

    @Query("SELECT * FROM alerts_hex ORDER BY id DESC LIMIT 1")
    fun latest(): Flow<HexAlertEntity?>

    @Query("SELECT * FROM alerts_hex ORDER BY id")
    fun current(): Flow<List<HexAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(alert: HexAlertEntity): Long

    @Query("DELETE FROM alerts_hex WHERE id = :alertId")
    suspend fun delete(alertId: AlertId): Int

    @Update
    suspend fun update(entity: HexAlertEntity): Int

    @Transaction
    suspend fun updateNoteIfDifferent(alertId: AlertId, newNote: String) {
        val entity = get(alertId) ?: throw IllegalArgumentException("No alert found for $alertId")

        if (entity.userNote == newNote) {
            log(AlertsDatabase.TAG) { "HexAlertEntity note is the same, not updating." }
            return
        }

        update(entity.copy(userNote = newNote))
    }
}