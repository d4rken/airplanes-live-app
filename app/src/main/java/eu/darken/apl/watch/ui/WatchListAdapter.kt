package eu.darken.apl.watch.ui

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
import eu.darken.apl.watch.ui.types.MultiAircraftWatchVH
import eu.darken.apl.watch.ui.types.SingleAircraftWatchVH
import javax.inject.Inject


class WatchListAdapter @Inject constructor() :
    ModularAdapter<WatchListAdapter.BaseVH<WatchListAdapter.Item, ViewBinding>>(),
    HasAsyncDiffer<WatchListAdapter.Item> {

    override val asyncDiffer: AsyncDiffer<*, Item> = setupDiffer()

    override fun getItemCount(): Int = data.size

    init {
        addMod(DataBinderMod(data))
        addMod(TypedVHCreatorMod({ data[it] is MultiAircraftWatchVH.Item }) { MultiAircraftWatchVH(it) })
        addMod(TypedVHCreatorMod({ data[it] is SingleAircraftWatchVH.Item }) { SingleAircraftWatchVH(it) })
    }

    abstract class BaseVH<Item : WatchListAdapter.Item, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : VH(layoutRes, parent), BindableVH<Item, VB>

    interface Item : DifferItem
}