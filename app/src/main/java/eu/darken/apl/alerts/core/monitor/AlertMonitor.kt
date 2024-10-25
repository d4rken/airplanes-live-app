package eu.darken.apl.alerts.core.monitor

import eu.darken.apl.alerts.core.AlertSettings
import eu.darken.apl.alerts.core.AlertsRepo
import eu.darken.apl.alerts.core.history.AlertHistoryRepo
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.search.core.SearchQuery
import eu.darken.apl.search.core.SearchRepo
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertMonitor @Inject constructor(
    private val settings: AlertSettings,
    private val alertsRepo: AlertsRepo,
    private val historyRepo: AlertHistoryRepo,
    private val searchRepo: SearchRepo,
) {
    suspend fun checkAlerts() {
        log(TAG) { "checkAlerts()" }
        alertsRepo.hexAlerts.first().let { alerts ->
            val results = searchRepo.search(SearchQuery.Hex(alerts.map { it.hex }.toSet()))
            // TODO filter for position
            alerts.forEach { alert ->
                val result = results.aircraft.firstOrNull { alert.matches(it) }
                historyRepo.addCheck(alert.id, if (result != null) 1 else 0)
            }
        }
        alertsRepo.callsignAlerts.first().let { alerts ->
            val results = searchRepo.search(SearchQuery.Callsign(alerts.map { it.callsign }.toSet()))
            // TODO filter for position
            alerts.forEach { alert ->
                val result = results.aircraft.firstOrNull { alert.matches(it) }
                historyRepo.addCheck(alert.id, if (result != null) 1 else 0)
            }
        }
        alertsRepo.squawkAlerts.first().let { alerts ->
            val results = searchRepo.search(SearchQuery.Squawk(alerts.map { it.code }.toSet()))
            // TODO filter for position
            alerts.forEach { alert ->
                val result = results.aircraft.firstOrNull { alert.matches(it) }
                historyRepo.addCheck(alert.id, if (result != null) 1 else 0)
            }
        }
    }

    companion object {
        private val TAG = logTag("Alerts", "Monitor")
    }
}