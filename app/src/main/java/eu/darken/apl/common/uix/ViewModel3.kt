package eu.darken.apl.common.uix

import androidx.navigation.NavDirections
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.asLog
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.flow.SingleEventFlow
import kotlinx.coroutines.CoroutineExceptionHandler


abstract class ViewModel3(
    dispatcherProvider: DispatcherProvider,
    override val tag: String = defaultTag(),
) : ViewModel2(dispatcherProvider) {

    val navEvents = SingleEventFlow<NavDirections?>()
    val errorEvents = SingleEventFlow<Throwable>()

    override var launchErrorHandler: CoroutineExceptionHandler? = CoroutineExceptionHandler { _, ex ->
        log(tag) { "Error during launch: ${ex.asLog()}" }
        errorEvents.emitBlocking(ex)
    }

    fun NavDirections.navigate() {
        navEvents.emitBlocking(this@navigate)
    }

    fun popNavStack() {
        navEvents.emitBlocking(null)
    }

    companion object {
        private fun defaultTag(): String = this::class.simpleName ?: "VM3"
    }
}