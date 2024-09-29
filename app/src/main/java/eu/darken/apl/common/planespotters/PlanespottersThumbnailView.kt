package eu.darken.apl.common.planespotters

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import eu.darken.apl.R
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.planespotters.coil.PlanespottersImage
import eu.darken.apl.databinding.CommonPlanespottersThumbnailViewBinding

class PlanespottersThumbnailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val ui = CommonPlanespottersThumbnailViewBinding.inflate(LayoutInflater.from(context), this)

    var onViewImageListener: ((PlanespottersMeta) -> Unit)? = null

    fun setImage(image: PlanespottersImage?) = ui.apply {
        log(VERBOSE) { "setImage($image)" }
        thumbnail.apply {
            setImageDrawable(image)
            isInvisible = image == null
        }
        author.apply {
            text = image?.meta?.author?.let { context.getString(R.string.general_photo_author_label, it) }
            isInvisible = image == null
        }
        if (image != null) {
            setOnClickListener { onViewImageListener?.invoke(image.meta) }
        } else {
            setOnClickListener(null)
        }
        loadingIndicator.isGone = image != null
    }

}