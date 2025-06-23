package eu.darken.apl.feeder.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.selection.SelectionTracker
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.common.lists.differ.update
import eu.darken.apl.common.lists.installListSelection
import eu.darken.apl.common.lists.setupDefaults
import eu.darken.apl.common.uix.Fragment3
import eu.darken.apl.common.viewbinding.viewBinding
import eu.darken.apl.databinding.FeederListFragmentBinding
import eu.darken.apl.main.ui.MainActivity
import eu.darken.apl.main.ui.main.MainFragmentDirections


@AndroidEntryPoint
class FeederListFragment : Fragment3(R.layout.feeder_list_fragment) {

    override val vm: FeederListViewModel by viewModels()
    override val ui: FeederListFragmentBinding by viewBinding()
    private var tracker: SelectionTracker<String>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.toolbar.apply {
            subtitle = resources.getQuantityString(R.plurals.feeder_yours_x_active_msg, 0, 0)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_add_feeder -> {
                        goToAddFeeder()
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

        val adapter = FeederListAdapter()
        ui.list.setupDefaults(adapter, verticalDividers = false)

        tracker = installListSelection(
            adapter = adapter,
            cabMenuRes = R.menu.menu_feeder_cab,
            onSelected = { tracker: SelectionTracker<String>, item: MenuItem, selected: Collection<FeederListAdapter.Item> ->
                if (selected.isEmpty()) return@installListSelection false

                when (item.itemId) {
                    R.id.menu_show_on_map_action -> {
                        vm.showFeedsOnMap(selected)
                        tracker.clearSelection()
                        true
                    }

                    else -> false
                }
            }
        )

        vm.state.observeWith(ui) { state ->
            swipeRefreshContainer.isInvisible = false
            loadingContainer.isGone = true

            emptyContainer.isVisible = state.items.isEmpty()
            mainAction.isVisible = state.items.isNotEmpty() && !state.isRefreshing

            swipeRefreshContainer.isRefreshing = state.isRefreshing

            adapter.update(state.items)
            toolbar.subtitle = resources.getQuantityString(R.plurals.feeder_yours_x_active_msg, 0, state.items.size)
        }

        ui.addFeederAction.setOnClickListener { goToAddFeeder() }

        ui.startFeedingAction.setOnClickListener { vm.startFeeding() }

        ui.mainAction.setOnClickListener { vm.refresh() }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        tracker?.clearSelection()
        super.onDestroyView()
    }

    private fun goToAddFeeder() {
        (requireActivity() as MainActivity).navController.navigate(
            MainFragmentDirections.actionMainFragmentToAddFeederFragment()
        )
    }
}
