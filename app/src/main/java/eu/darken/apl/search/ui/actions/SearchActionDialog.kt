package eu.darken.apl.search.ui.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.common.WebpageTool
import eu.darken.apl.common.planespotters.load
import eu.darken.apl.common.uix.BottomSheetDialogFragment2
import eu.darken.apl.databinding.SearchActionDialogBinding
import javax.inject.Inject

@AndroidEntryPoint
class SearchActionDialog : BottomSheetDialogFragment2() {

    override val vm: SearchActionViewModel by viewModels()
    override lateinit var ui: SearchActionDialogBinding
    @Inject lateinit var webpageTool: WebpageTool

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ui = SearchActionDialogBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        vm.state.observe2(ui) { state ->
            val aircraft = state.aircraft

            title.text = "${aircraft.label} (${aircraft.hex.uppercase()})"
            subtitle.apply {
                text = aircraft.description
                aircraft.operator?.let { append(" from $it") }
            }

//            distance.apply {
//                text = item?.distanceInMeter?.let {
//                    getString(R.string.general_xdistance_away_label, "${(it / 1000).toInt()}km")
//                }
//                isGone = item.distanceInMeter == null
//            }

            flightValue.text = aircraft.callsign ?: "?"
            squawkValue.text = aircraft.squawk ?: "?"

            thumbnail.apply {
                load(aircraft)
                onViewImageListener = { webpageTool.open(it.link) }
            }
        }

        ui.showMapAction.setOnClickListener { vm.showMap() }

        vm.events.observe2(ui) { event ->

        }

        super.onViewCreated(view, savedInstanceState)
    }
}