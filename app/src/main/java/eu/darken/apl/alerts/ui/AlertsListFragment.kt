package eu.darken.apl.alerts.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.common.lists.differ.update
import eu.darken.apl.common.lists.setupDefaults
import eu.darken.apl.common.uix.Fragment3
import eu.darken.apl.common.viewbinding.viewBinding
import eu.darken.apl.databinding.AlertsListFragmentBinding
import eu.darken.apl.main.ui.MainActivity


@AndroidEntryPoint
class AlertsListFragment : Fragment3(R.layout.alerts_list_fragment) {

    override val vm: AlertsListViewModel by viewModels()
    override val ui: AlertsListFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.toolbar.apply {
            subtitle = resources.getQuantityString(R.plurals.alerts_yours_x_active_msg, 0, 0)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_add_alert -> {
                        showAlertOptions()
                        true
                    }

                    R.id.action_settings -> {
                        (requireActivity() as MainActivity).goToSettings()
                        true
                    }

                    else -> false
                }
            }
        }

        ui.swipeRefreshContainer.setOnRefreshListener { vm.refresh() }

        val adapter = AlertsListAdapter()
        ui.list.setupDefaults(adapter, verticalDividers = false)

        vm.state.observe2(ui) { state ->
            swipeRefreshContainer.isInvisible = false
            loadingContainer.isGone = true

            emptyContainer.isVisible = state.items.isEmpty()
            mainAction.isVisible = state.items.isNotEmpty() && !state.isRefreshing

            swipeRefreshContainer.isRefreshing = state.isRefreshing

            adapter.update(state.items)
            toolbar.subtitle = resources.getQuantityString(R.plurals.alerts_yours_x_active_msg, 0, state.items.size)
        }

        ui.addAlertAction.setOnClickListener { showAlertOptions() }

        ui.mainAction.setOnClickListener { vm.refresh() }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun showAlertOptions() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.alerts_list_add_title)

            val options = arrayOf(
                getString(R.string.alerts_add_alert_type_label_callsign),
                getString(R.string.alerts_add_alert_type_label_hexcode),
                getString(R.string.alerts_add_alert_type_label_squawk),
            )
            setItems(options) { dialog, which ->
                when (which) {
                    0 -> AlertsListFragmentDirections.actionAlertsToCreateCallsignAlertFragment().navigate()
                    1 -> AlertsListFragmentDirections.actionAlertsToCreateHexAlertFragment().navigate()
                    2 -> AlertsListFragmentDirections.actionAlertsToCreateSquawkAlertFragment().navigate()
                }
                dialog.dismiss()
            }
            setNegativeButton(R.string.general_cancel_action) { _, _ -> }
        }.show()
    }

}
