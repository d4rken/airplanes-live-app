package testhelper

import eu.darken.apl.common.debug.logging.Logging
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.log
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterAll
import testhelpers.logging.JUnitLogger


open class BaseTest {
    init {
        Logging.clearAll()
        Logging.install(JUnitLogger())
        testClassName = this.javaClass.simpleName
    }

    companion object {
        const val IO_TEST_BASEDIR = "build/tmp/unit_tests"

        private var testClassName: String? = null

        @JvmStatic
        @AfterAll
        fun onTestClassFinished() {
            unmockkAll()
            log(testClassName!!, VERBOSE) { "onTestClassFinished()" }
            Logging.clearAll()
        }
    }
}
