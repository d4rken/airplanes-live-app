package eu.darken.apl.watch.core

import eu.darken.apl.watch.core.types.Watch
import kotlinx.coroutines.flow.first

suspend fun WatchRepo.getStatus(id: WatchId): Watch.Status? = status.first().singleOrNull { it.id == id }