package eu.darken.apl.common.uix

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.error.asErrorDialogBuilder
import eu.darken.apl.common.navigation.doNavigate
import eu.darken.apl.common.navigation.popBackStack
import kotlinx.coroutines.flow.Flow


abstract class Fragment3(@LayoutRes layoutRes: Int?) : Fragment2(layoutRes) {

    constructor() : this(null)

    open val ui: ViewBinding? = null
    abstract val vm: ViewModel3

    var onErrorEvent: ((Throwable) -> Boolean)? = null

    var onFinishEvent: (() -> Unit)? = null

    var customNavController: NavController? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.navEvents.observe {
            log { "navEvents: $it" }

            it?.run { doNavigate(this) } ?: onFinishEvent?.invoke() ?: popBackStack()
        }

        vm.errorEvents.observe {
            val showDialog = onErrorEvent?.invoke(it) ?: true
            if (showDialog) it.asErrorDialogBuilder(requireContext()).show()
        }
    }

    fun <T> Flow<T>.observe(collector: suspend (T) -> Unit) {
        observe(this@Fragment3, collector)
    }

    inline fun <T, reified VB> Flow<T>.observeWith(
        ui: VB,
        crossinline callback: VB.(T) -> Unit
    ) {
        observe { callback.invoke(ui, it) }
    }

    fun NavDirections.navigate() {
        doNavigate(this, customNavController ?: findNavController())
    }

    fun getTopLevelNavController(): NavController? {
        var parentFragment = parentFragment
        while (parentFragment != null) {
            if (parentFragment is NavHostFragment) {
                return parentFragment.navController

            }
            parentFragment = parentFragment.parentFragment
        }
        return null
    }
}
