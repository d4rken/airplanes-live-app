package eu.darken.apl.feeder.ui.types

import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import eu.darken.apl.R
import eu.darken.apl.common.lists.BindableVH
import eu.darken.apl.databinding.FeederListHeaderItemBinding
import eu.darken.apl.feeder.core.config.FeederSortMode
import eu.darken.apl.feeder.ui.FeederListAdapter

class FeederHeaderVH(parent: ViewGroup) :
    FeederListAdapter.BaseVH<FeederHeaderVH.Item, FeederListHeaderItemBinding>(
        R.layout.feeder_list_header_item,
        parent
    ), BindableVH<FeederHeaderVH.Item, FeederListHeaderItemBinding> {

    override val viewBinding = lazy { FeederListHeaderItemBinding.bind(itemView) }

    override val onBindData: FeederListHeaderItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        headerStatusText.text = if (item.hasOfflineFeeders) {
            context.getString(R.string.feeder_status_header_some_offline)
        } else {
            context.getString(R.string.feeder_status_header_all_online)
        }

        headerStatusText.setTextColor(
            if (item.hasOfflineFeeders) getColorForAttr(com.google.android.material.R.attr.colorError)
            else getColorForAttr(com.google.android.material.R.attr.colorPrimary)
        )

        // Setup sort mode spinner
        val sortModeOptions = listOf(
            SortModeOption(FeederSortMode.BY_LABEL, context.getString(R.string.feeder_sort_mode_by_label)),
            SortModeOption(FeederSortMode.BY_MESSAGE_RATE, context.getString(R.string.feeder_sort_mode_by_message_rate))
        )

        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            sortModeOptions.map { it.displayName }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        sortModeSpinner.adapter = adapter

        // Set the current selection without triggering the listener
        sortModeSpinner.setSelection(sortModeOptions.indexOfFirst { it.mode == item.currentSortMode })

        // Set up the listener for selection changes
        sortModeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedMode = sortModeOptions[position].mode
                if (selectedMode != item.currentSortMode) {
                    item.onSortModeSelected(selectedMode)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    data class SortModeOption(
        val mode: FeederSortMode,
        val displayName: String
    )

    data class Item(
        val hasOfflineFeeders: Boolean,
        val currentSortMode: FeederSortMode = FeederSortMode.BY_LABEL,
        val onSortModeSelected: (FeederSortMode) -> Unit = {}
    ) : FeederListAdapter.Item {
        override val stableId: Long = Item::class.java.hashCode().toLong()
    }
}
