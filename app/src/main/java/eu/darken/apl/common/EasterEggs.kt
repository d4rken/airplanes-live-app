package eu.darken.apl.common

import androidx.annotation.StringRes
import eu.darken.apl.R

@get:StringRes
val easterEggProgressMsg: Int
    get() = when ((0..7).random()) {
        0 -> R.string.generic_loading_label_0
        1 -> R.string.generic_loading_label_1
        2 -> R.string.generic_loading_label_2
        3 -> R.string.generic_loading_label_3
        4 -> R.string.generic_loading_label_4
        5 -> R.string.generic_loading_label_5
        6 -> R.string.generic_loading_label_6
        7 -> R.string.generic_loading_label_7
        else -> throw IllegalArgumentException()
    }