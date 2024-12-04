package eu.darken.apl.watch.core

import java.util.UUID

typealias WatchId = String


fun makeWatchId() = UUID.randomUUID().toString()