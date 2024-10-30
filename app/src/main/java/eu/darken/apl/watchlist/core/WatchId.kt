package eu.darken.apl.watchlist.core

import java.util.UUID

typealias WatchId = String

private const val HEX_SUFFIX = "hex"
private const val SQUAWK_SUFFIX = "squawk"
private const val CALLSIGN_SUFFIX = "callsign"
private const val DELIMITER = ":"

fun makeWatchIdForCallsign() = "${UUID.randomUUID()}$DELIMITER$CALLSIGN_SUFFIX"
fun makeWatchIdForHex() = "${UUID.randomUUID()}$DELIMITER$HEX_SUFFIX"
fun makeWatchIdForSquawk() = "${UUID.randomUUID()}$DELIMITER$SQUAWK_SUFFIX"

fun WatchId.isCallsign(): Boolean = this.endsWith("$DELIMITER$CALLSIGN_SUFFIX")
fun WatchId.isHex(): Boolean = this.endsWith("$DELIMITER$HEX_SUFFIX")
fun WatchId.isSquawk(): Boolean = this.endsWith("$DELIMITER$SQUAWK_SUFFIX")