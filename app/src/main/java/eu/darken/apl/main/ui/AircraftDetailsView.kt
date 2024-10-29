package eu.darken.apl.main.ui

import android.content.Context
import android.location.Location
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.planespotters.PlanespottersMeta
import eu.darken.apl.common.planespotters.load
import eu.darken.apl.databinding.CommonAircraftDetailsViewBinding
import eu.darken.apl.main.core.aircraft.Aircraft
import eu.darken.apl.main.core.aircraft.AircraftHex
import eu.darken.apl.main.core.aircraft.Airframe
import eu.darken.apl.main.core.aircraft.Callsign
import eu.darken.apl.main.core.aircraft.Registration
import eu.darken.apl.main.core.aircraft.SquawkCode
import java.time.Instant

class AircraftDetailsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val ui = CommonAircraftDetailsViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        if (isInEditMode) {
            setAircraft(
                object : Aircraft {
                    override val hex: AircraftHex
                        get() = "ABCDEF"
                    override val messageType: String
                        get() = "mlat"
                    override val dbFlags: Int?
                        get() = null
                    override val registration: Registration?
                        get() = null
                    override val callsign: Callsign?
                        get() = null
                    override val operator: String?
                        get() = null
                    override val airframe: Airframe?
                        get() = null
                    override val description: String?
                        get() = null
                    override val squawk: SquawkCode?
                        get() = null
                    override val emergency: String?
                        get() = null
                    override val outsideTemp: Int?
                        get() = null
                    override val altitude: String?
                        get() = null
                    override val altitudeRate: Int?
                        get() = null
                    override val groundSpeed: Float?
                        get() = null
                    override val indicatedAirSpeed: Int?
                        get() = null
                    override val trackheading: Double?
                        get() = null
                    override val location: Location?
                        get() = null
                    override val messages: Int
                        get() = 1000
                    override val seenAt: Instant
                        get() = Instant.now()
                    override val rssi: Double
                        get() = -90.0

                }
            )
        }
    }

    var onThumbnailClicked: ((PlanespottersMeta) -> Unit)? = null

    fun setAircraft(
        aircraft: Aircraft,
        distance: Float? = null
    ) = ui.apply {
        log(VERBOSE) { "setAircraft(${aircraft.hex}, $distance)" }

        airframe.text = aircraft.description
        operator.text = aircraft.operator

        firstValue.text = aircraft.callsign?.takeIf { it.isNotBlank() } ?: "?"
        secondValue.text = aircraft.registration ?: "?"
        thirdValue.text = "#${aircraft.hex.uppercase()}"
        fourthValue.text = aircraft.squawk ?: "?"
        fifthValue.text = "${aircraft.altitude ?: "?"} ft"
        sixthValue.text = "${aircraft.indicatedAirSpeed ?: aircraft.groundSpeed ?: "?"} kts"
        thumbnail.load(aircraft)

        thumbnail.onViewImageListener = { onThumbnailClicked?.invoke(it) }
    }
}