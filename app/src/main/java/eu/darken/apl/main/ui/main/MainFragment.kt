package eu.darken.apl.main.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_LABELED
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.common.datastore.valueBlocking
import eu.darken.apl.common.uix.Fragment3
import eu.darken.apl.common.viewbinding.viewBinding
import eu.darken.apl.databinding.MainFragmentBinding
import eu.darken.apl.main.core.GeneralSettings
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment3(R.layout.main_fragment) {

    override val vm: MainViewModel by viewModels()
    override val ui: MainFragmentBinding by viewBinding()
    @Inject lateinit var generalSettings: GeneralSettings


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!generalSettings.isOnboardingFinished.valueBlocking) {
            MainFragmentDirections.actionMainFragmentToWelcomeFragment().navigate()
            return
        }

        vm.newRelease.observe2(ui) { release ->
            Snackbar
                .make(
                    requireView(),
                    "New release available",
                    Snackbar.LENGTH_LONG
                )
                .setAction("Show") {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(release.htmlUrl)
                    }
                    requireActivity().startActivity(intent)
                }
                .apply {
                    anchorView = ui.bottomNavigation
                }
                .show()
        }

        val navController: NavController = ui.bottomNavHost.getFragment<NavHostFragment>().navController
        ui.bottomNavigation.apply {
            setupWithNavController(navController)
            labelVisibilityMode = LABEL_VISIBILITY_LABELED
            setOnItemSelectedListener { item ->
                if (navController.currentDestination?.id == item.itemId) {
                    return@setOnItemSelectedListener true
                }
                when (item.itemId) {
                    R.id.search -> {
                        if (!navController.popBackStack(R.id.search, false)) {
                            navController.navigate(R.id.search)
                        }
                        true
                    }

                    R.id.map -> {
                        navController.navigate(R.id.map)
                        true
                    }

                    R.id.alerts -> {
                        navController.navigate(R.id.alerts)
                        true
                    }

                    R.id.feeder -> {
                        navController.navigate(R.id.feeder)
                        true
                    }

                    else -> false
                }
            }
        }

        vm.isInternetAvailable.observe2(ui) {
            offlineContainer.isGone = it
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
