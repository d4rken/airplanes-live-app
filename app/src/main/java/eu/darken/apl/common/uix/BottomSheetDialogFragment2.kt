package eu.darken.apl.common.uix

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.error.asErrorDialogBuilder
import eu.darken.apl.common.navigation.doNavigate
import eu.darken.apl.common.navigation.popBackStack
import kotlinx.coroutines.flow.Flow


abstract class BottomSheetDialogFragment2 : BottomSheetDialogFragment() {

    abstract val ui: ViewBinding
    abstract val vm: ViewModel3

    internal val tag: String =
        logTag("Fragment", "${this.javaClass.simpleName}(${Integer.toHexString(hashCode())})")

    override fun onAttach(context: Context) {
        log(tag, VERBOSE) { "onAttach(context=$context)" }
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        log(tag, VERBOSE) { "onCreate(savedInstanceState=$savedInstanceState)" }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        log(tag, VERBOSE) {
            "onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState"
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        log(tag, VERBOSE) { "onViewCreated(view=$view, savedInstanceState=$savedInstanceState)" }
        super.onViewCreated(view, savedInstanceState)

        vm.navEvents.observe { dir -> dir?.let { doNavigate(it) } ?: popBackStack() }
        vm.errorEvents.observe { it.asErrorDialogBuilder(requireContext()).show() }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        log(tag, VERBOSE) { "onActivityCreated(savedInstanceState=$savedInstanceState)" }
        super.onActivityCreated(savedInstanceState)
    }

    override fun onResume() {
        log(tag, VERBOSE) { "onResume()" }
        super.onResume()
    }

    override fun onPause() {
        log(tag, VERBOSE) { "onPause()" }
        super.onPause()
    }

    override fun onDestroyView() {
        log(tag, VERBOSE) { "onDestroyView()" }
        super.onDestroyView()
    }

    override fun onDetach() {
        log(tag, VERBOSE) { "onDetach()" }
        super.onDetach()
    }

    override fun onDestroy() {
        log(tag, VERBOSE) { "onDestroy()" }
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        log(tag, VERBOSE) { "onActivityResult(requestCode=$requestCode, resultCode=$resultCode, data=$data)" }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun <T> Flow<T>.observe(collector: suspend (T) -> Unit) {
        observe(this@BottomSheetDialogFragment2, collector)
    }

    inline fun <T, reified VB> Flow<T>.observeWith(
        ui: VB,
        crossinline callback: VB.(T) -> Unit
    ) {
        observe { callback.invoke(ui, it) }
    }
}
