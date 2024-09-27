package eu.darken.apl.common.planespotters.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path

interface PlanespottersApi {

    @JsonClass(generateAdapter = true)
    data class PhotosResponse(
        @Json(name = "photos") val photos: List<Photo>
    )

    @JsonClass(generateAdapter = true)
    data class Photo(
        @Json(name = "id") val id: String,
        @Json(name = "thumbnail") val thumbnail: Thumbnail,
        @Json(name = "thumbnail_large") val thumbnailLarge: Thumbnail,
        @Json(name = "link") val link: String,
        @Json(name = "photographer") val photographer: String
    ) {

        @JsonClass(generateAdapter = true)
        data class Thumbnail(
            @Json(name = "src") val src: String,
            @Json(name = "size") val size: Size
        ) {
            @JsonClass(generateAdapter = true)
            data class Size(
                @Json(name = "width") val width: Int,
                @Json(name = "height") val height: Int
            )
        }
    }


    @GET("pub/photos/hex/{hexCode}")
    suspend fun getPhotosByHex(@Path("hexCode") hexCode: String): PhotosResponse

    @GET("pub/photos/reg/{registration}")
    suspend fun getPhotosByRegistration(@Path("registration") registration: String): PhotosResponse

}