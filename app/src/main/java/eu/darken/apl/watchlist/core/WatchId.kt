package eu.darken.apl.watchlist.core

import java.util.UUID

typealias WatchId = String


fun makeWatchId() = UUID.randomUUID().toString()