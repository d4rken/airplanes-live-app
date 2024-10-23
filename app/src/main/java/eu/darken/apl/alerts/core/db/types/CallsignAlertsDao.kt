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
interface CallsignAlertsDao {
    @Query("SELECT * FROM alerts_callsign WHERE id = :alertId")
    suspend fun get(alertId: AlertId): CallsignAlertEntity?

    @Query("SELECT * FROM alerts_callsign")
    fun getAll(): List<CallsignAlertEntity>

    @Query("SELECT * FROM alerts_callsign ORDER BY id DESC LIMIT 1")
    fun firehose(): Flow<CallsignAlertEntity?>

    @Query("SELECT * FROM alerts_callsign ORDER BY id")
    fun current(): Flow<List<CallsignAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(alert: CallsignAlertEntity): Long

    @Query("DELETE FROM alerts_callsign WHERE id = :alertId")
    suspend fun delete(alertId: AlertId): Int

    @Update
    suspend fun update(entity: CallsignAlertEntity): Int

    @Transaction
    suspend fun updateNoteIfDifferent(alertId: AlertId, newNote: String) {
        val entity = get(alertId) ?: throw IllegalArgumentException("No alert found for $alertId")

        if (entity.userNote == newNote) {
            log(AlertsDatabase.TAG) { "CallsignAlertEntity Note is the same, not updating." }
            return
        }

        update(entity.copy(userNote = newNote))
    }
}