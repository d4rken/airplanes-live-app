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
interface WatchDao {
    @Query("SELECT * FROM watch_base WHERE id = :watchId")
    suspend fun get(watchId: WatchId): BaseWatchEntity?

    @Query("SELECT * FROM watch_base ORDER BY id DESC LIMIT 1")
    fun latest(): Flow<BaseWatchEntity?>

    @Query("SELECT * FROM watch_base ORDER BY id")
    fun current(): Flow<List<BaseWatchEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(watch: BaseWatchEntity): Long

    @Query("DELETE FROM watch_base WHERE id = :watchId")
    suspend fun delete(watchId: WatchId): Int

    @Update
    suspend fun update(entity: BaseWatchEntity): Int

    @Transaction
    suspend fun updateNoteIfDifferent(watchId: WatchId, newNote: String) {
        val entity = get(watchId) ?: throw IllegalArgumentException("No watch found for $watchId")

        if (entity.userNote == newNote) {
            log(WatchlistDatabase.TAG) { "AircraftWatchEntity note is the same, not updating." }
            return
        }

        update(entity.copy(userNote = newNote))
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(aircraft: AircraftWatchEntity): Long

    @Transaction
    suspend fun insert(base: BaseWatchEntity, aircraft: AircraftWatchEntity) {
        insert(base)
        insert(aircraft)
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(flight: FlightWatchEntity): Long

    @Transaction
    suspend fun insert(base: BaseWatchEntity, flight: FlightWatchEntity) {
        insert(base)
        insert(flight)
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(squawk: SquawkWatchEntity): Long

    @Transaction
    suspend fun insert(base: BaseWatchEntity, squawk: SquawkWatchEntity) {
        insert(base)
        insert(squawk)
    }

    @Transaction
    suspend fun <T : WatchType> insert(base: BaseWatchEntity, related: T) {
        insert(base)
        when (related) {
            is AircraftWatchEntity -> insert(related)
            is FlightWatchEntity -> insert(related)
//            is SquawkWatchEntity -> insert(related)
//            else -> throw IllegalArgumentException("Unknown related entity type.")
        }
    }
}