package eu.darken.apl.main.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_LABELED
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

        vm.newRelease.observeWith(ui) { release ->
            val apkUrl = release.assets.find { it.name.endsWith(".apk", ignoreCase = true) }?.downloadUrl

            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.update_available_dialog_title)
                setMessage(R.string.update_available_dialog_message)
                setPositiveButton(R.string.update_available_show_action) { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = release.htmlUrl.toUri()
                    }
                    requireActivity().startActivity(intent)
                }
                if (apkUrl != null) {
                    setNegativeButton(R.string.update_available_download_action) { _, _ ->
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = apkUrl.toUri()
                        }
                        requireActivity().startActivity(intent)
                    }
                }
                setNeutralButton(R.string.common_dismiss_action) { _, _ -> }
            }.show()
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
                        if (!navController.popBackStack(R.id.map, false)) {
                            navController.navigate(R.id.map)
                        }
                        true
                    }

                    R.id.watchlist -> {
                        if (!navController.popBackStack(R.id.watchlist, false)) {
                            navController.navigate(R.id.watchlist)
                        }
                        true
                    }

                    R.id.feeder -> {
                        if (!navController.popBackStack(R.id.feeder, false)) {
                            navController.navigate(R.id.feeder)
                        }
                        true
                    }

                    else -> false
                }
            }
        }

        vm.isInternetAvailable.observeWith(ui) {
            offlineContainer.isGone = it
        }

        super.onViewCreated(view, savedInstanceState)
    }
}