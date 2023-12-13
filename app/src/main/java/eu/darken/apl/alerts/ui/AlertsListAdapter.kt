package eu.darken.apl.alerts.ui

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import eu.darken.apl.alerts.ui.types.HexAlertVH
import eu.darken.apl.alerts.ui.types.SquawkAlertVH
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.common.lists.differ.AsyncDiffer
import eu.darken.apl.common.lists.differ.DifferItem
import eu.darken.apl.common.lists.differ.HasAsyncDiffer
import eu.darken.apl.common.lists.differ.setupDiffer
import eu.darken.apl.common.lists.modular.ModularAdapter
import eu.darken.apl.common.lists.modular.mods.DataBinderMod
import eu.darken.apl.common.lists.modular.mods.TypedVHCreatorMod
import javax.inject.Inject


class AlertsListAdapter @Inject constructor() :
    ModularAdapter<AlertsListAdapter.BaseVH<AlertsListAdapter.Item, ViewBinding>>(),
    HasAsyncDiffer<AlertsListAdapter.Item> {

    override val asyncDiffer: AsyncDiffer<*, Item> = setupDiffer()

    override fun getItemCount(): Int = data.size

    init {
        modules.add(DataBinderMod(data))
        modules.add(TypedVHCreatorMod({ data[it] is HexAlertVH.Item }) { HexAlertVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is SquawkAlertVH.Item }) { SquawkAlertVH(it) })
    }

    abstract class BaseVH<Item : AlertsListAdapter.Item, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : VH(layoutRes, parent), BindableVH<Item, VB>

    interface Item : DifferItem
}