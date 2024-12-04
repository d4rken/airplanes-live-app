package eu.darken.apl.common.planespotters.coil

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
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
        image.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        image.colorFilter = colorFilter
    }

    override fun onBoundsChange(bounds: Rect) {
        image.bounds = bounds
        super.onBoundsChange(bounds)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("image.opacity"))
    override fun getOpacity(): Int {
        return image.opacity
    }

    val raw: Bitmap?
        get() = (image as? BitmapDrawable)?.bitmap
}