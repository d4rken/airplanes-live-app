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
interface AircraftWatchDao {
    @Query("SELECT * FROM watch_aircraft WHERE id = :watchId")
    suspend fun get(watchId: WatchId): AircraftWatchEntity?

    @Query("SELECT * FROM watch_aircraft ORDER BY id DESC LIMIT 1")
    fun latest(): Flow<AircraftWatchEntity?>

    @Query("SELECT * FROM watch_aircraft ORDER BY id")
    fun current(): Flow<List<AircraftWatchEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(watch: AircraftWatchEntity): Long

    @Query("DELETE FROM watch_aircraft WHERE id = :watchId")
    suspend fun delete(watchId: WatchId): Int

    @Update
    suspend fun update(entity: AircraftWatchEntity): Int

    @Transaction
    suspend fun updateNoteIfDifferent(watchId: WatchId, newNote: String) {
        val entity = get(watchId) ?: throw IllegalArgumentException("No watch found for $watchId")

        if (entity.userNote == newNote) {
            log(WatchlistDatabase.TAG) { "AircraftWatchEntity note is the same, not updating." }
            return
        }

        update(entity.copy(userNote = newNote))
    }
}