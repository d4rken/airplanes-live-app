package eu.darken.apl.common.planespotters.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

interface PlanespottersApi {

    @Serializable
    data class PhotosResponse(
        @SerialName("photos") val photos: List<Photo>
    )

    @Serializable
    data class Photo(
        @SerialName("id") val id: String,
        @SerialName("thumbnail") val thumbnail: Thumbnail,
        @SerialName("thumbnail_large") val thumbnailLarge: Thumbnail,
        @SerialName("link") val link: String,
        @SerialName("photographer") val photographer: String
    ) {

        @Serializable
        data class Thumbnail(
            @SerialName("src") val src: String,
            @SerialName("size") val size: Size
        ) {
            @Serializable
            data class Size(
                @SerialName("width") val width: Int,
                @SerialName("height") val height: Int
            )
        }
    }


    @GET("pub/photos/hex/{hexCode}")
    suspend fun getPhotosByHex(@Path("hexCode") hexCode: String): PhotosResponse

    @GET("pub/photos/reg/{registration}")
    suspend fun getPhotosByRegistration(@Path("registration") registration: String): PhotosResponse

}
