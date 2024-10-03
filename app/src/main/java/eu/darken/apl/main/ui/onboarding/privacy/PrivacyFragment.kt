package eu.darken.apl.main.ui.onboarding.privacy

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.common.WebpageTool
import eu.darken.apl.common.setChecked2
import eu.darken.apl.common.uix.Fragment3
import eu.darken.apl.common.viewbinding.viewBinding
import eu.darken.apl.databinding.PrivacyFragmentBinding
import javax.inject.Inject


@AndroidEntryPoint
class PrivacyFragment : Fragment3(R.layout.privacy_fragment) {

    override val vm: PrivacyViewModel by viewModels()
    override val ui: PrivacyFragmentBinding by viewBinding()
    @Inject lateinit var webpageTool: WebpageTool

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.privacyPolicyAction.setOnClickListener { vm.goPrivacyPolicy() }
        ui.goAction.setOnClickListener { vm.finishPrivacy() }

        ui.updateContainer.setOnClickListener { vm.toggleUpdateCheck() }

        vm.state.observe2(ui) { state ->
            updateToggle.setChecked2(state.isUpdateCheckEnabled, false)
            updateContainer.isVisible = state.isUpdateCheckSupported
        }
        super.onViewCreated(view, savedInstanceState)
    }
}
