package eu.darken.apl.map.ui

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.uix.Fragment3
import eu.darken.apl.common.viewbinding.viewBinding
import eu.darken.apl.databinding.MapFragmentBinding
import eu.darken.apl.main.ui.MainActivity
import eu.darken.apl.map.core.WebMapClient
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@AndroidEntryPoint
class MapFragment : Fragment3(R.layout.map_fragment) {

    override val vm: MapViewModel by viewModels()
    override val ui: MapFragmentBinding by viewBinding()

    @Inject lateinit var webMapClientFactory: WebMapClient.Factory

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            log(TAG) { "locationPermissionLauncher: $isGranted" }
            if (isGranted) vm.homeMap()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedcallback)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.toolbar.apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_settings -> {
                        (requireActivity() as MainActivity).goToSettings()
                        true
                    }

                    R.id.action_reload_map -> {
                        ui.webview.reload()
                        true
                    }

                    else -> false
                }
            }
        }

        val webMapClient = webMapClientFactory.create(ui.webview).apply {
            events
                .onEach { event ->
                    when (event) {
                        WebMapClient.AppInterface.Event.HomePressed -> vm.checkLocationPermission()
                    }
                }
                .launchIn(viewLifecycleOwner.lifecycleScope)
        }

        vm.state.observe2(ui) { state ->
            webview.loadUrl(state.url)
        }

        vm.events.observe2 { event ->
            when (event) {
                MapEvents.RequestLocationPermission -> {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }

                MapEvents.HomeMap -> {
                    webMapClient.clickHome()
                }
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        ui.webview.onResume()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        ui.webview.onPause()
    }

    private val onBackPressedcallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            ui.webview.apply {
                if (canGoBack()) goBack()
            }
        }
    }

    companion object {
        private val TAG = logTag("Map", "Fragment")
    }
}
