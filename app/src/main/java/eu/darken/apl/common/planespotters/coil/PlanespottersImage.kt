package eu.darken.apl.common.planespotters.coil

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Rect
import android.graphics.drawable.Drawable
import eu.darken.apl.common.planespotters.PlanespottersMeta

data class PlanespottersImage(
    val image: Drawable,
    val meta: PlanespottersMeta,
) : Drawable() {
    override fun draw(canvas: Canvas) {
        image.draw(canvas)
    }

    override fun setAlpha(alpha: Int) {
        image.setAlpha(alpha)
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        image.setColorFilter(colorFilter)
    }

    override fun onBoundsChange(bounds: Rect) {
        image.bounds = bounds
        super.onBoundsChange(bounds)
    }

    override fun getOpacity(): Int {
        return image.opacity
    }
}