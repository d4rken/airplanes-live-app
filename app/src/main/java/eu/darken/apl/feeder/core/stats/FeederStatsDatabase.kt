package eu.darken.apl.feeder.core.stats

import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeederStatsDatabase @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val database by lazy {
        Room.databaseBuilder(
            context,
            FeederStatsRoomDb::class.java, "feeder-stats"
        ).build()
    }

    val beastStats: BeastStatsDao
        get() = database.beastStats()

    val mlatStats: MlatStatsDao
        get() = database.mlatStats()
}