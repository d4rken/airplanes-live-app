package eu.darken.apl.map.ui

sealed interface MapEvents {

    data object RequestLocationPermission : MapEvents
    data object HomeMap : MapEvents

}