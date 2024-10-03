package eu.darken.apl.main.ui.onboarding.welcome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.common.uix.Fragment3
import eu.darken.apl.common.viewbinding.viewBinding
import eu.darken.apl.databinding.WelcomeFragmentBinding


@AndroidEntryPoint
class WelcomeFragment : Fragment3(R.layout.welcome_fragment) {

    override val vm: WelcomeViewModel by viewModels()
    override val ui: WelcomeFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.goAction.setOnClickListener { vm.finishWelcome() }
        super.onViewCreated(view, savedInstanceState)
    }
}
