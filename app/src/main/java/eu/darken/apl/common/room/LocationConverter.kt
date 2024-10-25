package eu.darken.apl.common.room

import android.location.Location
import androidx.room.TypeConverter

class LocationConverter {
    @TypeConverter
    fun fromLocation(location: Location?): String? = location?.let {
        "${it.latitude},${it.longitude},${it.provider}"
    }

    @TypeConverter
    fun toLocation(locationString: String?): Location? = locationString?.let {
        val parts = it.split(",")
        if (parts.size == 3) {
            Location(parts[2]).apply {
                latitude = parts[0].toDouble()
                longitude = parts[1].toDouble()
            }
        } else {
            null
        }
    }
}