package eu.darken.apl.common.planespotters.api

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import eu.darken.apl.common.ca.caString
import eu.darken.apl.common.error.HasLocalizedError
import eu.darken.apl.common.error.LocalizedError
import retrofit2.HttpException

class PlanespottersException(
    val identifier: String,
    private val error: HttpException,
) : Exception("Planespotters.net error for $identifier: $error"), HasLocalizedError {

    private val adapter by lazy {
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        moshi.adapter<Map<String, Any>>(type)
    }

    private val errorMap by lazy {
        error.response()?.errorBody()?.string()?.let { adapter.fromJson(it) } ?: emptyMap()
    }

    override fun getLocalizedError(context: Context): LocalizedError = LocalizedError(
        throwable = this,
        label = caString { "Planespotters.net error" },
        description = caString { errorMap["error"]?.toString() ?: error.toString() }
    )
}