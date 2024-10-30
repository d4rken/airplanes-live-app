package eu.darken.apl.watchlist.core.db.types

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.watchlist.core.WatchId
import eu.darken.apl.watchlist.core.db.WatchlistDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface SquawkWatchDao {
    @Query("SELECT * FROM watch_squawk WHERE id = :watchId")
    suspend fun get(watchId: WatchId): SquawkWatchEntity?

    @Query("SELECT * FROM watch_squawk")
    fun getAll(): List<SquawkWatchEntity>

    @Query("SELECT * FROM watch_squawk ORDER BY id DESC LIMIT 1")
    fun firehose(): Flow<SquawkWatchEntity?>

    @Query("SELECT * FROM watch_squawk ORDER BY id")
    fun current(): Flow<List<SquawkWatchEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(alert: SquawkWatchEntity): Long

    @Query("DELETE FROM watch_squawk WHERE id = :watchId")
    suspend fun delete(watchId: WatchId): Int

    @Update
    suspend fun update(entity: SquawkWatchEntity): Int

    @Transaction
    suspend fun updateNoteIfDifferent(watchId: WatchId, newNote: String) {
        val entity = get(watchId) ?: throw IllegalArgumentException("No watch found for $watchId")

        if (entity.userNote == newNote) {
            log(WatchlistDatabase.TAG) { "SquawkWatchEntity Note is the same, not updating." }
            return
        }

        update(entity.copy(userNote = newNote))
    }
}