package eu.darken.apl.watchlist.core.db.types

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import eu.darken.apl.main.core.aircraft.SquawkCode
import eu.darken.apl.watchlist.core.WatchId

@Entity(
    tableName = "watch_squawk",
    foreignKeys = [
        ForeignKey(
            entity = BaseWatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SquawkWatchEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: WatchId,
    @ColumnInfo(name = "code") val code: SquawkCode,
) : WatchType {
    companion object {
        const val TYPE_KEY = "squawk"
    }
}