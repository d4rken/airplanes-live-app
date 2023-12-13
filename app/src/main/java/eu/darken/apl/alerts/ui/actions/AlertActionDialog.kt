package eu.darken.apl.alerts.ui.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.alerts.core.types.HexAlert
import eu.darken.apl.alerts.core.types.SquawkAlert
import eu.darken.apl.common.uix.BottomSheetDialogFragment2
import eu.darken.apl.databinding.AlertsActionDialogBinding

@AndroidEntryPoint
class AlertActionDialog : BottomSheetDialogFragment2() {
    override val vm: AlertActionViewModel by viewModels()
    override lateinit var ui: AlertsActionDialogBinding

    private val permissionlauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ui = AlertsActionDialogBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        vm.state.observe2(ui) { (alert) ->
            when (alert) {
                is HexAlert -> {
                    icon.setImageResource(R.drawable.ic_hexagon_multiple_24)
                    primary.text = alert.label
                    secondary.text = alert.hex
                    tertiary.text = alert.infos.firstOrNull()?.description
                        ?: getString(R.string.alerts_item_hexcode_subtitle)
                }

                is SquawkAlert -> {
                    icon.setImageResource(R.drawable.ic_router_wireless_24)
                    primary.text = alert.squawk
                    secondary.text = getString(R.string.alerts_item_squawk_subtitle)
                    tertiary.text = resources.getQuantityString(
                        R.plurals.alerts_squawk_matches_msg,
                        alert.infos.size,
                        alert.infos.size,
                        alert.squawk
                    )
                }
            }
        }

        ui.showFeedAction.setOnClickListener { vm.showOnMap() }
        ui.removeFeederAction.setOnClickListener { vm.removeAlert() }


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