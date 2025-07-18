package eu.darken.apl.common

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import java.io.File
import java.util.UUID
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstallId @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val installIDFile = File(context.filesDir, INSTALL_ID_FILENAME)
    val id: String by lazy {
        val existing = if (installIDFile.exists()) {
            installIDFile.readText().also {
                if (!UUID_PATTERN.matcher(it).matches()) throw IllegalStateException("Invalid InstallID: $it")
            }
        } else {
            null
        }

        return@lazy existing ?: UUID.randomUUID().toString().also {
            log(TAG) { "New install ID created: $it" }
            installIDFile.writeText(it)
        }
    }

    companion object {
        private val TAG: String = logTag("InstallID")
        private val UUID_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"
        )

        private const val INSTALL_ID_FILENAME = "installid"
    }
}

