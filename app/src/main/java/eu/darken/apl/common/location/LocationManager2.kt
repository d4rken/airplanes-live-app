package eu.darken.apl.common.location

import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.apl.common.coroutine.AppScope
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.flow.replayingShare
import eu.darken.apl.common.permissions.Permission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationManager2 @Inject constructor(
    @AppScope private val scope: CoroutineScope,
    @ApplicationContext private val context: Context,
    private val locationManager: LocationManager,
) {
    private val executor: Executor = Executors.newSingleThreadExecutor()

    private val permissionFlow: Flow<Boolean> = flow {
        var lastCheck: Boolean? = null
        fun check() = Permission.ACCESS_COARSE_LOCATION.isGranted(context)
        while (currentCoroutineContext().isActive) {
            val newCheck = check()
            if (newCheck != lastCheck) emit(newCheck)
            lastCheck = newCheck
            delay(3000L)
        }
    }
    private val locationFlow: Flow<State> = callbackFlow {
        if (!Permission.ACCESS_COARSE_LOCATION.isGranted(context)) {
            throw SecurityException("ACCESS_COARSE_LOCATION has not been granted")
        }

        val locationListener = LocationListenerCompat { location ->
            log(TAG) { "New location $location" }
            trySend(State.Available(location))
        }

        log(TAG) { "Requesting location updates" }
        @Suppress("MissingPermission")
        LocationManagerCompat.requestLocationUpdates(
            locationManager,
            LocationManager.NETWORK_PROVIDER,
            LocationRequestCompat.Builder(60 * 1000L).apply {
                setMinUpdateDistanceMeters(100f)
                setQuality(LocationRequestCompat.QUALITY_BALANCED_POWER_ACCURACY)
            }.build(),
            executor,
            locationListener
        )

        awaitClose {
            log(TAG) { "Canceling location updates" }
            @Suppress("MissingPermission")
            LocationManagerCompat.removeUpdates(locationManager, locationListener)
        }
    }

    val state = permissionFlow
        .flatMapLatest { hasPermission ->
            if (hasPermission) {
                locationFlow
            } else {
                flowOf(State.Unavailable(SecurityException("ACCESS_COARSE_LOCATION has not been granted")))
            }
        }
        .catch {
            emit(State.Unavailable(error = it))
        }
        .replayingShare(scope)


    sealed interface State {
        data class Available(
            val location: Location,
        ) : State

        data class Unavailable(
            val error: Throwable,
        ) : State {
            val isPermissionIssue: Boolean
                get() = error is SecurityException
        }
    }

    companion object {
        private val TAG = logTag("LocationManager2")
    }
}