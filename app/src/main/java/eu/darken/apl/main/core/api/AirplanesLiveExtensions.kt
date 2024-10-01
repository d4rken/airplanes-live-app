package eu.darken.apl.main.core.api

import android.location.Location


suspend fun AirplanesLiveEndpoint.getByLocation(location: Location, radius: Float) =
    getByLocation(location.latitude, location.longitude, radius)