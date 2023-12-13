package eu.darken.apl.main.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.common.PrivacyPolicy
import eu.darken.apl.common.WebpageTool
import eu.darken.apl.common.uix.Fragment3
import eu.darken.apl.common.viewbinding.viewBinding
import eu.darken.apl.databinding.OnboardingFragmentBinding
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingFragment : Fragment3(R.layout.onboarding_fragment) {

    override val vm: OnboardingViewModel by viewModels()
    override val ui: OnboardingFragmentBinding by viewBinding()
    @Inject lateinit var webpageTool: WebpageTool

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        customNavController = requireActivity().findNavController(R.id.nav_host)
        ui.goPrivacyPolicy.setOnClickListener { webpageTool.open(PrivacyPolicy.URL) }
        ui.continueAction.setOnClickListener { vm.finishOnboarding() }
        super.onViewCreated(view, savedInstanceState)
    }
}
