package eu.darken.apl.main.ui.settings

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.common.uix.Fragment3
import eu.darken.apl.common.viewbinding.viewBinding
import eu.darken.apl.databinding.SettingsFragmentBinding
import kotlinx.parcelize.Parcelize

@AndroidEntryPoint
class SettingsFragment : Fragment3(R.layout.settings_fragment),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override val vm: SettingsViewModel by viewModels()
    override val ui: SettingsFragmentBinding by viewBinding()

    val toolbar: Toolbar
        get() = ui.toolbar

    private val screens = ArrayList<Screen>()

    @Parcelize
    data class Screen(
        val fragmentClass: String,
        val screenTitle: String?
    ) : Parcelable

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        childFragmentManager.addOnBackStackChangedListener {
            val backStackCnt = childFragmentManager.backStackEntryCount
            val newScreenInfo = when {
                backStackCnt < screens.size -> {
                    // We popped the backstack, restore the underlying screen infos
                    // If there are none left, we are at the index again
                    screens.removeLastOrNull()
                    screens.lastOrNull() ?: Screen(
                        fragmentClass = SettingsIndexFragment::class.qualifiedName!!,
                        screenTitle = getString(R.string.label_settings)
                    )
                }

                else -> {
                    // We added the current fragment to the stack, the new fragment's infos were already set, do nothing.
                    null
                }
            }

            newScreenInfo?.let { setCurrentScreenInfo(it) }
        }

        if (savedInstanceState == null) {
            childFragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, SettingsIndexFragment())
                .commit()
        } else {
            savedInstanceState.getParcelableArrayList<Screen>(BKEY_SCREEN_INFOS)?.let {
                screens.addAll(it)
            }
            screens.lastOrNull()?.let { setCurrentScreenInfo(it) }
        }

        ui.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        super.onViewCreated(view, savedInstanceState)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(BKEY_SCREEN_INFOS, screens)
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val screenInfo = Screen(
            fragmentClass = pref.fragment!!,
            screenTitle = pref.title?.toString()
        )

        val args = Bundle().apply {
            putAll(pref.extras)
            putString(BKEY_SCREEN_TITLE, screenInfo.screenTitle)
        }

        val fragment = childFragmentManager.fragmentFactory
            .instantiate(this::class.java.classLoader!!, pref.fragment!!)
            .apply {
                arguments = args
                setTargetFragment(caller, 0)
            }

        setCurrentScreenInfo(screenInfo)
        screens.add(screenInfo)

        childFragmentManager.beginTransaction().apply {
            replace(R.id.content_frame, fragment)
            addToBackStack(null)
        }.commit()

        return true
    }


    private fun setCurrentScreenInfo(info: Screen) {
        ui.toolbar.apply {
            title = info.screenTitle
        }
    }

    companion object {
        private const val BKEY_SCREEN_TITLE = "preferenceScreenTitle"
        private const val BKEY_SCREEN_INFOS = "preferenceScreenInfos"
    }
}
