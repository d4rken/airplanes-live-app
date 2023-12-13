package eu.darken.apl.common.debug.recorder.ui

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import eu.darken.apl.R
import eu.darken.apl.common.PrivacyPolicy
import eu.darken.apl.common.WebpageTool

class RecorderConsentDialog(
    private val context: Context,
    private val webpageTool: WebpageTool
) {
    fun showDialog(onStartRecord: () -> Unit) {
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.support_debuglog_label)
            setMessage(R.string.settings_debuglog_explanation)
            setPositiveButton(R.string.debug_debuglog_record_action) { _, _ -> onStartRecord() }
            setNegativeButton(R.string.general_cancel_action) { _, _ -> }
            setNeutralButton(R.string.settings_privacy_policy_label) { _, _ ->
                webpageTool.open(PrivacyPolicy.URL)
            }
        }.show()
    }
}