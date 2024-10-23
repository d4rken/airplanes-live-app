package eu.darken.apl.alerts.ui.create

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.databinding.AlertsCreateHexBinding

@AndroidEntryPoint
class CreateHexAlertFragment : DialogFragment() {

    private val vm: CreateHexAlertViewModel by viewModels()

    private val binding by lazy {
        AlertsCreateHexBinding.inflate(layoutInflater, null, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.alerts_add_hexcode_title)
            setMessage(R.string.alerts_add_hexcode_msg)
            setView(binding.root)

            setPositiveButton(R.string.general_add_action) { _, _ ->
                vm.create(
                    binding.inputValue.text.toString(),
                    binding.commentValue.text.toString(),
                )
            }
            setNegativeButton(R.string.general_cancel_action) { dialog, _ -> dialog.dismiss() }
        }.create().also { dialog ->
            dialog.setOnShowListener {
                val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.isEnabled = false
                binding.inputValue.addTextChangedListener { text ->
                    positiveButton.isEnabled = (text?.length ?: 0) > 5
                }

                binding.inputValue.setText(vm.initHex ?: "")
                binding.commentValue.setText(vm.initNote ?: "")
            }
        }

}

