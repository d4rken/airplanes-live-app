package eu.darken.apl.common.network

import kotlinx.coroutines.flow.first

suspend fun NetworkStateProvider.hasInternet() = networkState.first().isInternetAvailable