package eu.darken.apl.alerts.core

import eu.darken.apl.alerts.core.config.AlertSettings
import eu.darken.apl.alerts.core.config.db.AlertsDatabase
import eu.darken.apl.alerts.core.config.db.HexAlertEntity
import eu.darken.apl.alerts.core.config.db.SquawkAlertEntity
import eu.darken.apl.alerts.core.types.AircraftAlert
import eu.darken.apl.alerts.core.types.HexAlert
import eu.darken.apl.alerts.core.types.SquawkAlert
import eu.darken.apl.common.coroutine.AppScope
import eu.darken.apl.common.debug.logging.Logging.Priority.INFO
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.flow.replayingShare
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.aircraft.SquawkCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
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
) {

    private val refreshTrigger = MutableStateFlow(UUID.randomUUID())
    val isRefreshing = MutableStateFlow(false)
    private val lock = Mutex()

    val status: Flow<Collection<AircraftAlert.Status>> = combine(
        refreshTrigger,
        alertsDatabase.squawkAlerts.current(),
        alertsDatabase.hexAlerts.current(),
    ) { _, squawkAlerts, hexAlerts ->
        val status = mutableSetOf<AircraftAlert.Status>()
        squawkAlerts
            .map { entity -> SquawkAlert(entity) }
            .map {
                SquawkAlert.Status(
                    alert = it,
                )
            }
            .run {
                log(TAG) { "Got ${this.size} squawk alerts:\n${this.joinToString("\n")}" }
                status.addAll(this)
            }
        hexAlerts
            .map { entity -> HexAlert(entity) }
            .map {
                HexAlert.Status(
                    alert = it,
                )
            }
            .run {
                log(TAG) { "Got ${this.size} hex alerts:\n${this.joinToString("\n")}" }
                status.addAll(this)
            }
        status
    }
        .replayingShare(appScope)

    suspend fun refresh() {
        log(TAG) { "refresh()" } // TODO
        refreshTrigger.value = UUID.randomUUID()
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

                    else -> throw IllegalArgumentException("Invalid alertId: $id")
                }
            }
        }
    }

    companion object {
        private val TAG = logTag("Alerts", "Repo")
    }
}