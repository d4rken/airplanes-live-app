package eu.darken.apl.common.error

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Throwable.asErrorDialogBuilder(
    context: Context
) = MaterialAlertDialogBuilder(context).apply {
    val error = this@asErrorDialogBuilder
    val localizedError = error.localized(context)

    setTitle(localizedError.label.get(context))
    setMessage(localizedError.description.get(context))

    setPositiveButton(android.R.string.ok) { _, _ ->

    }
}