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
import eu.darken.apl.databinding.AlertsCreateSquawkBinding

@AndroidEntryPoint
class CreateSquawkAlertFragment : DialogFragment() {

    private val vm: CreateSquawkAlertViewModel by viewModels()

    private val binding by lazy {
        AlertsCreateSquawkBinding.inflate(layoutInflater, null, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.alerts_add_squawk_title)
            setMessage(R.string.alerts_add_squawk_msg)
            setView(binding.root)
            setPositiveButton(R.string.general_add_action) { _, _ ->
                vm.create(
                    code = binding.inputValue.text.toString(),
                    note = binding.commentValue.text.toString(),
                )
            }
            setNegativeButton(R.string.general_cancel_action) { dialog, _ -> dialog.dismiss() }
        }.create().also { dialog ->
            dialog.setOnShowListener {
                val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.isEnabled = false
                binding.inputValue.addTextChangedListener {
                    positiveButton.isEnabled = (it?.length ?: 0) == 4
                }

                binding.inputValue.setText(vm.initSquawk ?: "")
                binding.commentValue.setText(vm.initNote ?: "")
            }
        }

}

