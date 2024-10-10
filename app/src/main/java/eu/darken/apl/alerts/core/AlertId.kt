package eu.darken.apl.alerts.core

import java.util.UUID

typealias AlertId = String

private const val HEX_SUFFIX = "hex"
private const val SQUAWK_SUFFIX = "squawk"
private const val DELIMITER = ":"

fun makeAlertIdForHex() = "${UUID.randomUUID()}$DELIMITER$HEX_SUFFIX"
fun makeAlertIdForSquawk() = "${UUID.randomUUID()}$DELIMITER$SQUAWK_SUFFIX"

fun AlertId.isHex(): Boolean = this.endsWith("$DELIMITER$HEX_SUFFIX")
fun AlertId.isSquawk(): Boolean = this.endsWith("$DELIMITER$SQUAWK_SUFFIX")