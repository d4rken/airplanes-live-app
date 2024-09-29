package eu.darken.apl.common.planespotters.coil

import coil.key.Keyer
import coil.request.Options
import eu.darken.apl.main.core.aircraft.Aircraft

class PlanespottersKeyer : Keyer<Aircraft> {
    override fun key(data: Aircraft, options: Options): String {
        return "aircraft-${data.hex}"
    }
}