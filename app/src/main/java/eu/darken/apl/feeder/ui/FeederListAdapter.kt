package eu.darken.apl.feeder.ui

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.common.lists.differ.AsyncDiffer
import eu.darken.apl.common.lists.differ.DifferItem
import eu.darken.apl.common.lists.differ.HasAsyncDiffer
import eu.darken.apl.common.lists.differ.setupDiffer
import eu.darken.apl.common.lists.modular.ModularAdapter
import eu.darken.apl.common.lists.modular.mods.DataBinderMod
import eu.darken.apl.common.lists.modular.mods.TypedVHCreatorMod
import eu.darken.apl.common.lists.selection.SelectableItem
import eu.darken.apl.feeder.ui.types.DefaultFeederVH
import javax.inject.Inject


class FeederListAdapter @Inject constructor() :
    ModularAdapter<FeederListAdapter.BaseVH<FeederListAdapter.Item, ViewBinding>>(),
    HasAsyncDiffer<FeederListAdapter.Item> {

    override val asyncDiffer: AsyncDiffer<*, Item> = setupDiffer()

    override fun getItemCount(): Int = data.size

    init {
        addMod(DataBinderMod(data))
        addMod(TypedVHCreatorMod({ data[it] is DefaultFeederVH.Item }) { DefaultFeederVH(it) })
    }

    abstract class BaseVH<Item : FeederListAdapter.Item, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : VH(layoutRes, parent), BindableVH<Item, VB>

    interface Item : DifferItem, SelectableItem {
        override val itemSelectionKey: String?
            get() = null
    }
}