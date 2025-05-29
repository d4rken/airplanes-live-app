package eu.darken.apl.map.core

import eu.darken.apl.feeder.core.ReceiverId

fun ReceiverId.toMapFeedId() = split("-").take(3).joinToString("")