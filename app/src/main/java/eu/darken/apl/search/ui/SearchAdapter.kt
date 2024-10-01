package eu.darken.apl.search.ui

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
import eu.darken.apl.search.ui.items.AircraftResultVH
import eu.darken.apl.search.ui.items.LocationPromptVH
import eu.darken.apl.search.ui.items.NoAircraftVH
import eu.darken.apl.search.ui.items.SearchingAircraftVH
import eu.darken.apl.search.ui.items.SummaryVH
import javax.inject.Inject


class SearchAdapter @Inject constructor() :
    ModularAdapter<SearchAdapter.BaseVH<SearchAdapter.Item, ViewBinding>>(),
    HasAsyncDiffer<SearchAdapter.Item> {

    override val asyncDiffer: AsyncDiffer<*, Item> = setupDiffer()

    override fun getItemCount(): Int = data.size

    init {
        modules.add(DataBinderMod(data))
        modules.add(TypedVHCreatorMod({ data[it] is LocationPromptVH.Item }) { LocationPromptVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is SearchingAircraftVH.Item }) { SearchingAircraftVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is NoAircraftVH.Item }) { NoAircraftVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is AircraftResultVH.Item }) { AircraftResultVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is SummaryVH.Item }) { SummaryVH(it) })
    }

    abstract class BaseVH<Item : SearchAdapter.Item, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : VH(layoutRes, parent), BindableVH<Item, VB>

    interface Item : DifferItem
}