package eu.darken.apl.common.lists.selection

import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

interface SelectableVH {
    val itemSelectionKey: String?
        get() = null

    fun updatedSelectionState(selected: Boolean) {

    }
}

fun <T> T.getItemDetails(): ItemDetailsLookup.ItemDetails<String>
        where T : SelectableVH, T : RecyclerView.ViewHolder =
    object : ItemDetailsLookup.ItemDetails<String>() {
        override fun getPosition(): Int = bindingAdapterPosition
        override fun getSelectionKey(): String? = itemSelectionKey
    }