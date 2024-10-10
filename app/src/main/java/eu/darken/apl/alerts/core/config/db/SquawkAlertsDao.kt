package eu.darken.apl.alerts.core.config.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import eu.darken.apl.alerts.core.AlertId
import eu.darken.apl.common.debug.logging.log
import kotlinx.coroutines.flow.Flow

@Dao
interface SquawkAlertsDao {
    @Query("SELECT * FROM alerts_squawk WHERE id = :alertId")
    suspend fun get(alertId: AlertId): SquawkAlertEntity?

    @Query("SELECT * FROM alerts_squawk")
    fun getAll(): List<SquawkAlertEntity>

    @Query("SELECT * FROM alerts_squawk ORDER BY id DESC LIMIT 1")
    fun firehose(): Flow<SquawkAlertEntity?>

    @Query("SELECT * FROM alerts_squawk ORDER BY id")
    fun current(): Flow<List<SquawkAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(alert: SquawkAlertEntity): Long

    @Query("DELETE FROM alerts_squawk WHERE id = :alertId")
    suspend fun delete(alertId: AlertId): Int

    @Update
    suspend fun update(entity: SquawkAlertEntity): Int

    @Transaction
    suspend fun updateNoteIfDifferent(alertId: AlertId, newNote: String) {
        val entity = get(alertId) ?: throw IllegalArgumentException("No alert found for $alertId")

        if (entity.userNote == newNote) {
            log(AlertsDatabase.TAG) { "SquawkAlertEntity Note is the same, not updating." }
            return
        }

        update(entity.copy(userNote = newNote))
    }
}