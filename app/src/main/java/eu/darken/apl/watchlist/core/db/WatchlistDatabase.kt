package eu.darken.apl.watchlist.core.db

import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.watchlist.core.db.history.WatchCheckDao
import eu.darken.apl.watchlist.core.db.types.WatchDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistDatabase @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val database by lazy {
        Room.databaseBuilder(
            context,
            WatchlistRoomDb::class.java, "watchlist"
        ).build()
    }

    val watches: WatchDao
        get() = database.watches()

    val checks: WatchCheckDao
        get() = database.checks()

    companion object {
        internal val TAG = logTag("Watchlist", "Database")
    }
}