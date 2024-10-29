package eu.darken.apl.common

import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StringRes


fun View.getString(@StringRes id: Int): String = resources.getString(id)

fun View.getString(@StringRes id: Int, vararg formatArgs: Any?): String = resources.getString(id, *formatArgs)

@ColorInt
fun View.getColorForAttr(@AttrRes id: Int) = context.getColorForAttr(id)