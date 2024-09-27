package eu.darken.apl.common.planespotters

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.databinding.CommonPlanespottersThumbnailViewBinding

class PlanespottersThumbnailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val ui = CommonPlanespottersThumbnailViewBinding.inflate(LayoutInflater.from(context), this)

    fun setImage(image: PlanespottersImage) {
        log(VERBOSE) { "setImage($image)" }
    }

}