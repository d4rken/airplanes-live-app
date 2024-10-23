package eu.darken.apl.alerts.core.db.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eu.darken.apl.alerts.core.AlertId
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertCheckDao {
    @Query("SELECT * FROM alert_checks WHERE id = :id")
    suspend fun get(id: String): AlertCheckEntity?

    @Query("SELECT * FROM alert_checks WHERE alert_id = :alertId ORDER BY checked_at DESC LIMIT 1")
    suspend fun getLastCheck(alertId: String): AlertCheckEntity?

    @Query("SELECT * FROM alert_checks WHERE alert_id = :alertId AND aircraft_count > 0 ORDER BY checked_at DESC LIMIT 1")
    suspend fun getLastHit(alertId: String): AlertCheckEntity?

    @Query("SELECT * FROM alert_checks")
    fun getAll(): List<AlertCheckEntity>

    @Query("SELECT * FROM alert_checks ORDER BY checked_at DESC LIMIT 1")
    fun firehose(): Flow<AlertCheckEntity?>

    @Query("SELECT * FROM alert_checks ORDER BY checked_at")
    fun current(): Flow<List<AlertCheckEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(alert: AlertCheckEntity): Long

    @Query("DELETE FROM alert_checks WHERE id = :alertId")
    suspend fun deleteForAlert(alertId: AlertId): Int
}