package eu.darken.apl.alerts.core

import eu.darken.apl.alerts.core.db.AlertsDatabase
import eu.darken.apl.alerts.core.db.types.CallsignAlertEntity
import eu.darken.apl.alerts.core.db.types.HexAlertEntity
import eu.darken.apl.alerts.core.db.types.SquawkAlertEntity
import eu.darken.apl.alerts.core.history.AlertHistoryRepo
import eu.darken.apl.alerts.core.types.AircraftAlert
import eu.darken.apl.alerts.core.types.CallsignAlert
import eu.darken.apl.alerts.core.types.HexAlert
import eu.darken.apl.alerts.core.types.SquawkAlert
import eu.darken.apl.common.coroutine.AppScope
import eu.darken.apl.common.debug.logging.Logging.Priority.INFO
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.flow.combine
import eu.darken.apl.common.flow.replayingShare
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.aircraft.Callsign
import eu.darken.apl.main.core.aircraft.SquawkCode
import eu.darken.apl.search.core.SearchRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertsRepo @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val settings: AlertSettings,
    private val alertsDatabase: AlertsDatabase,
    private val alertsHistory: AlertHistoryRepo,
    private val searchRepo: SearchRepo,
) {

    private val refreshTrigger = MutableStateFlow(UUID.randomUUID())
    val isRefreshing = MutableStateFlow(false)
    private val lock = Mutex()

    val hexAlerts: Flow<List<HexAlert>> = alertsDatabase.hexAlerts.current()
        .map { alerts -> alerts.map { HexAlert(it) } }
        .replayingShare(appScope)
    val callsignAlerts: Flow<List<CallsignAlert>> = alertsDatabase.callsignAlerts.current()
        .map { alerts -> alerts.map { CallsignAlert(it) } }
        .replayingShare(appScope)
    val squawkAlerts: Flow<List<SquawkAlert>> = alertsDatabase.squawkAlerts.current()
        .map { alerts -> alerts.map { SquawkAlert(it) } }
        .replayingShare(appScope)

    val status: Flow<Collection<AircraftAlert.Status>> = combine(
        refreshTrigger,
        alertsHistory.firehose,
        searchRepo.cache,
        hexAlerts,
        callsignAlerts,
        squawkAlerts,
    ) { _, _, searchCache, hexAlerts, callsignAlerts, squawkAlerts ->
        log(TAG) { "Search cache size ${searchCache.size}" }

        val status = mutableSetOf<AircraftAlert.Status>()
        hexAlerts
            .map { alert ->
                HexAlert.Status(
                    alert = alert,
                    lastCheck = alertsHistory.getLastCheck(alert.id),
                    lastHit = alertsHistory.getLastHit(alert.id),
                    tracked = searchCache.values
                        .filter { it.hex == alert.hex }
                        .toSet()
                        .also { if (it.isNotEmpty()) log(TAG) { "Matched $alert to $it" } }
                )
            }
            .run {
                log(TAG) { "Got ${this.size} hex alerts:\n${this.joinToString("\n")}" }
                status.addAll(this)
            }
        callsignAlerts
            .map { alert ->
                CallsignAlert.Status(
                    alert = alert,
                    lastCheck = alertsHistory.getLastCheck(alert.id),
                    lastHit = alertsHistory.getLastHit(alert.id),
                    tracked = searchCache.values
                        .filter { it.callsign == alert.callsign }
                        .toSet()
                        .also { if (it.isNotEmpty()) log(TAG) { "Matched $alert to $it" } }
                )
            }
            .run {
                log(TAG) { "Got ${this.size} callsign alerts:\n${this.joinToString("\n")}" }
                status.addAll(this)
            }
        squawkAlerts
            .map { alert ->
                SquawkAlert.Status(
                    alert = alert,
                    lastCheck = alertsHistory.getLastCheck(alert.id),
                    lastHit = alertsHistory.getLastHit(alert.id),
                    tracked = searchCache.values
                        .filter { it.squawk == alert.code }
                        .toSet()
                        .also { if (it.isNotEmpty()) log(TAG) { "Matched $alert to $it" } }
                )
            }
            .run {
                log(TAG) { "Got ${this.size} squawk alerts:\n${this.joinToString("\n")}" }
                status.addAll(this)
            }
        status
    }
        .replayingShare(appScope)

    suspend fun refresh() {
        log(TAG) { "refresh()" } // TODO
        refreshTrigger.value = UUID.randomUUID()
    }

    suspend fun createCallsignAlert(callsign: Callsign, note: String = ""): CallsignAlert {
        log(TAG) { "createCallsignAlert($callsign, $note)" }
        val entity = CallsignAlertEntity(
            callsign = callsign,
            userNote = note,
        )
        alertsDatabase.callsignAlerts.insert(entity)
        return CallsignAlert(
            entity = entity,
        ).also {
            log(TAG, INFO) { "createCallsignAlert(...): Created $it" }
        }
    }

    suspend fun createHexAlert(hex: AircraftHex, note: String = ""): HexAlert {
        log(TAG) { "createHexAlert($hex, $note)" }
        val entity = HexAlertEntity(
            hexCode = hex,
            userNote = note,
        )
        alertsDatabase.hexAlerts.insert(entity)
        return HexAlert(
            entity = entity,
        ).also {
            log(TAG, INFO) { "createHexAlert(...): Created $it" }
        }
    }

    suspend fun createSquawkAlert(code: SquawkCode, note: String = "") {
        log(TAG) { "createSquawkAlert($code, $note)" }

        lock.withLock {
            withContext(NonCancellable) {
                val alertEntity = SquawkAlertEntity(
                    code = code,
                    userNote = note,
                )
                alertsDatabase.squawkAlerts.insert(alertEntity)
            }
        }

        refresh()
    }

    suspend fun deleteAlert(id: AlertId) = lock.withLock {
        log(TAG) { "deleteAlert($id)" }

        withContext(NonCancellable) {
            when {
                id.isHex() -> {
                    alertsDatabase.hexAlerts.delete(id)
                    log(TAG) { "deleteAlert(...): Deleted hex $id" }
                }

                id.isSquawk() -> {
                    alertsDatabase.squawkAlerts.delete(id)
                    log(TAG) { "deleteAlert(...): Deleted squawk $id" }
                }

                id.isCallsign() -> {
                    alertsDatabase.callsignAlerts.delete(id)
                    log(TAG) { "deleteAlert(...): Deleted callsign $id" }
                }

                else -> throw IllegalArgumentException("Invalid alertId: $id")
            }
        }

        refresh()
    }

    suspend fun updateNote(id: AlertId, note: String) = lock.withLock {
        log(TAG) { "updateNote($id,$note)" }

        withContext(NonCancellable) {
            alertsDatabase.run {
                when {
                    id.isHex() -> hexAlerts.updateNoteIfDifferent(id, note)

                    id.isSquawk() -> squawkAlerts.updateNoteIfDifferent(id, note)

                    id.isCallsign() -> callsignAlerts.updateNoteIfDifferent(id, note)

                    else -> throw IllegalArgumentException("Invalid alertId: $id")
                }
            }
        }
    }

    companion object {
        private val TAG = logTag("Alerts", "Repo")
    }
}