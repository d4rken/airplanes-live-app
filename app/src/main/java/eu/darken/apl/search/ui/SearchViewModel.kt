package eu.darken.apl.search.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.uix.ViewModel3
import eu.darken.apl.search.core.SearchRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    @Suppress("UNUSED_PARAMETER") handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val searchRepo: SearchRepo
) : ViewModel3(dispatcherProvider) {

    private val currentQuery = MutableStateFlow<SearchRepo.Query?>(null)
    private val currentSearch: Flow<SearchRepo.Result?> = currentQuery
        .map { query -> query?.let { searchRepo.search(it) } }
        .flatMapLatest { it ?: flowOf(null) }

    val state = combine(
        currentQuery,
        currentSearch,
    ) { query, result ->
        val items = mutableListOf<SearchAdapter.Item>()

        result?.aircraft?.map { ac ->
            SearchResultVH.Item(
                aircraft = ac,
                distanceInMeter = 123 * 1000L,
                onTap = {

                },
                onLongPress = {

                },
            )
        }?.run { items.addAll(this) }

        State(
            query = query,
            isSearching = result?.searching ?: false,
            items = items,
        )
    }.asLiveData2()

    fun search(term: String?) {
        log(TAG) { "search($term)" }
        currentQuery.value = term
            ?.takeIf { it.isNotBlank() }
            ?.let { SearchRepo.Query(it) }
    }

    data class State(
        val items: List<SearchAdapter.Item>,
        val query: SearchRepo.Query? = null,
        val isSearching: Boolean = false,
    )

    companion object {
        private val TAG = logTag("Search", "Fragment", "ViewModel")
    }
}