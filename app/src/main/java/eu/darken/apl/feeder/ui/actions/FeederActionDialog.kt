package eu.darken.apl.feeder.ui.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.common.hasApiLevel
import eu.darken.apl.common.permissions.Permission
import eu.darken.apl.common.uix.BottomSheetDialogFragment2
import eu.darken.apl.databinding.FeederActionDialogBinding

@AndroidEntryPoint
class FeederActionDialog : BottomSheetDialogFragment2() {
    override val vm: FeederActionViewModel by viewModels()
    override lateinit var ui: FeederActionDialogBinding

    private val permissionlauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ui = FeederActionDialogBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        vm.state.observe2(ui) { (feeder) ->
            primary.text = feeder.label
            secondary.text = feeder.id
            tertiary.text = "\uD83C\uDF7B"
            monitorFeederOfflineToggle.isChecked = feeder.config.offlineCheckTimeout != null
        }

        ui.monitorFeederOfflineToggle.apply {
            setOnClickListener {
                if (hasApiLevel(33) && !Permission.POST_NOTIFICATIONS.isGranted(requireContext())) {
                    permissionlauncher.launch(Permission.POST_NOTIFICATIONS.permissionId)
                }
                vm.toggleNotifyWhenOffline()
            }
        }

        ui.showFeedAction.setOnClickListener { vm.showFeedOnMap() }
        ui.removeFeederAction.setOnClickListener { vm.removeFeeder() }


        vm.events.observe2(ui) { event ->
            when (event) {
                is FeederActionEvents.RemovalConfirmation -> MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle(R.string.feeder_remove_confirmation_title)
                    setMessage(R.string.feeder_remove_confirmation_message)
                    setPositiveButton(R.string.general_remove_action) { _, _ -> vm.removeFeeder(confirmed = true) }
                    setNegativeButton(R.string.general_cancel_action) { _, _ -> }
                }.show()
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }
}