package eu.darken.apl.alerts.core.types

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import eu.darken.apl.R
import eu.darken.apl.common.error.HasLocalizedError
import eu.darken.apl.common.error.LocalizedError
import retrofit2.HttpException

class UnsupportedSquawkError(
    private val error: HttpException,
) : IllegalArgumentException("Provided squawk not allowed"), HasLocalizedError {

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
        label = context.getString(R.string.alerts_squawk_error_notallowed_label),
        description = context.getString(R.string.alerts_squawk_error_notallowed_message, errorMap["allowed"])
    )
}