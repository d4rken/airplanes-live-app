package eu.darken.apl.common.github

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import java.time.OffsetDateTime

interface GithubApi {

    @JsonClass(generateAdapter = true)
    data class ReleaseInfo(
        @Json(name = "name") val name: String,
        @Json(name = "tag_name") val tagName: String,
        @Json(name = "html_url") val htmlUrl: String,
        @Json(name = "published_at") val publishedAt: OffsetDateTime,
        @Json(name = "body") val body: String,
        @Json(name = "assets") val assets: List<Asset>,
    ) {
        @JsonClass(generateAdapter = true)
        data class Asset(
            @Json(name = "id") val id: Long,
            @Json(name = "name") val name: String,
            @Json(name = "label") val label: String,
            @Json(name = "size") val size: Long,
            @Json(name = "content_type") val contentType: String,
            @Json(name = "browser_download_url") val downloadUrl: String,
        )
    }

    @GET("/repos/{owner}/{repo}/releases/latest")
    suspend fun latestRelease(@Path("owner") owner: String, @Path("repo") repo: String): ReleaseInfo
}