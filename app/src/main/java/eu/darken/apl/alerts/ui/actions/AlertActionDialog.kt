package eu.darken.apl.alerts.ui.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.alerts.core.types.CallsignAlert
import eu.darken.apl.alerts.core.types.HexAlert
import eu.darken.apl.alerts.core.types.SquawkAlert
import eu.darken.apl.common.uix.BottomSheetDialogFragment2
import eu.darken.apl.databinding.AlertsActionDialogBinding

@AndroidEntryPoint
class AlertActionDialog : BottomSheetDialogFragment2() {
    override val vm: AlertActionViewModel by viewModels()
    override lateinit var ui: AlertsActionDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ui = AlertsActionDialogBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.primaryEditAction.setOnClickListener {

        }
        ui.noteInput.addTextChangedListener {
            vm.updateNote(it.toString())
        }

        ui.showMapAction.setOnClickListener { vm.showOnMap() }
        ui.showSearchAction.setOnClickListener { vm.showInSearch() }
        ui.removeFeederAction.setOnClickListener { vm.removeAlert() }

        vm.state.observe2(ui) { (status) ->
            when (status) {
                is HexAlert.Status -> {
                    icon.setImageResource(R.drawable.ic_hexagon_multiple_24)
                    primary.text = status.hex
                    secondary.text = getString(R.string.alerts_item_hexcode_subtitle)
                }

                is CallsignAlert.Status -> {
                    icon.setImageResource(R.drawable.ic_bullhorn_24)
                    primary.text = status.callsign
                    secondary.text = getString(R.string.alerts_item_callsign_subtitle)
                }

                is SquawkAlert.Status -> {
                    icon.setImageResource(R.drawable.ic_router_wireless_24)
                    primary.text = status.squawk
                    secondary.text = getString(R.string.alerts_item_squawk_subtitle)
                }
            }

            if (noteInput.text?.isEmpty() == true) {
                noteInput.setText(status.note)
            }
        }

        vm.events.observe2(ui) { event ->
            when (event) {
                is AlertActionEvents.RemovalConfirmation -> MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle(R.string.alerts_remove_confirmation_title)
                    setMessage(R.string.alerts_remove_confirmation_message)
                    setPositiveButton(R.string.general_remove_action) { _, _ -> vm.removeAlert(confirmed = true) }
                    setNegativeButton(R.string.general_cancel_action) { _, _ -> }
                }.show()
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }
}