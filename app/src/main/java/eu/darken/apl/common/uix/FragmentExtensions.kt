package eu.darken.apl.common.uix

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


fun <T> Flow<T>.observe(fragment: Fragment, collector: suspend (T) -> Unit) {
    fragment.viewLifecycleOwner.lifecycleScope.launch {
        fragment.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            collect(collector)
        }
    }
}

fun <T> Flow<T>.observe(activity: ComponentActivity, collector: suspend (T) -> Unit) {
    activity.lifecycleScope.launch {
        activity.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            collect(collector)
        }
    }
}