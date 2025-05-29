package eu.darken.apl.feeder.ui.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.common.hasApiLevel
import eu.darken.apl.common.permissions.Permission
import eu.darken.apl.common.uix.BottomSheetDialogFragment2
import eu.darken.apl.databinding.CommonTextinputDialogBinding
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

        vm.state.observeWith(ui) { (feeder) ->
            primary.text = feeder.label
            secondary.text = feeder.id
            tertiary.text = "Host-address: ${feeder.config.address ?: "\uD83C\uDF7B"}"
            monitorFeederOfflineToggle.isChecked = feeder.config.offlineCheckTimeout != null
            addressActions.isGone = feeder.config.address == null
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
        ui.renameAction.setOnClickListener { vm.renameFeeder() }
        ui.setAddressAction.setOnClickListener { vm.changeAddress() }
        ui.addressTar1090Action.setOnClickListener { vm.openTar1090() }
        ui.addressGraphs1090Action.setOnClickListener { vm.openGraphs1090() }
        ui.removeFeederAction.setOnClickListener { vm.removeFeeder() }


        vm.events.observeWith(ui) { event ->
            when (event) {
                is FeederActionEvents.RemovalConfirmation -> MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle(R.string.feeder_remove_confirmation_title)
                    setMessage(R.string.feeder_remove_confirmation_message)
                    setPositiveButton(R.string.common_remove_action) { _, _ -> vm.removeFeeder(confirmed = true) }
                    setNegativeButton(R.string.common_cancel_action) { _, _ -> }
                }.show()

                is FeederActionEvents.Rename -> MaterialAlertDialogBuilder(requireContext()).apply {
                    val layout = CommonTextinputDialogBinding.inflate(layoutInflater, null, false).apply {
                        input.hint = "T-EDKA123"
                        input.setText(event.feeder.label)
                    }
                    setTitle(R.string.feeder_name_change_title)
                    setMessage(R.string.feeder_name_change_body)
                    setView(layout.root)
                    setPositiveButton(R.string.feeder_name_change_action) { _, _ ->
                        vm.renameFeeder(layout.input.text.toString())
                    }
                    setNegativeButton(R.string.common_cancel_action) { dialog, _ ->
                        dialog.dismiss()
                    }
                }.show()

                is FeederActionEvents.ChangeIpAddress -> MaterialAlertDialogBuilder(requireContext()).apply {
                    val layout = CommonTextinputDialogBinding.inflate(layoutInflater, null, false).apply {
                        input.hint = "192.168.0.42/myfeeder.domain.tld"
                        input.setText(event.feeder.config.address)
                    }
                    setTitle(R.string.feeder_name_change_title)
                    setMessage(R.string.feeder_name_change_body)
                    setView(layout.root)
                    setPositiveButton(R.string.feeder_name_change_action) { _, _ ->
                        vm.changeAddress(layout.input.text.toString())
                    }
                    setNegativeButton(R.string.common_cancel_action) { dialog, _ ->
                        dialog.dismiss()
                    }
                }.show()
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }
}