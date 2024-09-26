package eu.darken.apl.search.core

import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.main.core.api.AplApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepo @Inject constructor() {


    data class Query(
        val term: String,
        val type: Type = Type.ALL,
    ) {
        enum class Type {
            HEX, SQUAWK, ALL
        }
    }

    data class Result(
        val aircraft: Collection<AplApi.Aircraft>,
        val searching: Boolean,
    )

    suspend fun search(query: Query): Flow<Result> {
        log(TAG) { "search($query)" }
        return flow {

        }
    }

    companion object {
        private val TAG = logTag("Search", "Repo")
    }
}