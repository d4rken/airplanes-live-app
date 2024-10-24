package eu.darken.apl.common.lists

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.setupDefaults(adapter: RecyclerView.Adapter<*>? = null, dividers: Boolean = true) = apply {
    layoutManager = LinearLayoutManager(context)
    itemAnimator = DefaultItemAnimator()
    if (dividers) {
        addItemDecoration(
            DividerItemDecoration2(
                context,
                DividerItemDecoration.VERTICAL,
                drawAfterLastItem = false
            )
        )
    }
    if (adapter != null) this.adapter = adapter
}