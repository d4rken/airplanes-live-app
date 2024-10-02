package eu.darken.apl.common.planespotters.coil

import androidx.appcompat.content.res.AppCompatResources
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.ImageRequest
import coil.request.Options
import coil.request.SuccessResult
import coil.size.pxOrElse
import eu.darken.apl.R
import eu.darken.apl.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.planespotters.PlanespottersMeta
import eu.darken.apl.common.planespotters.api.PlanespottersEndpoint
import eu.darken.apl.main.core.aircraft.Aircraft
import retrofit2.HttpException
import javax.inject.Inject

class PlanespottersFetcher(
    private val aircraft: Aircraft,
    private val options: Options,
    private val imageLoader: ImageLoader,
    private val endpoint: PlanespottersEndpoint,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        log(TAG, VERBOSE) { "Fetching $aircraft with $options " }

        val photos = endpoint.getPhotosByHex(aircraft.hex)
        log(TAG, VERBOSE) { "Got ${photos.size} photos for $aircraft with $options, picking first. " }

        // TODO maybe a slideshow of pictures?
        val photo = photos.firstOrNull() ?: return DrawableResult(
            drawable = PlanespottersImage(
                AppCompatResources.getDrawable(options.context, R.drawable.aircraft_photo_unavailable)!!,
                PlanespottersMeta(
                    author = "?",
                    link = "https://www.planespotters.net",
                ),
            ),
            isSampled = false,
            dataSource = DataSource.MEMORY,
        )

        val request = ImageRequest.Builder(options.context).apply {
            val largeHeight = options.size.height.pxOrElse { 128 } > photo.thumbnail.size.height
            val largeWidth = options.size.width.pxOrElse { 128 } > photo.thumbnail.size.width
            val bestSize = if (largeHeight || largeWidth) {
                log(TAG) { "Picking large thumbnail" }
                photo.thumbnailLarge
            } else {
                log(TAG) { "Picking small thumbnail" }
                photo.thumbnail
            }
            data(bestSize.src)
            size(options.size)
            log(TAG) { "OPTION SIZE: ${options.size}" }
        }.build()

        val result = try {
            imageLoader.execute(request)
        } catch (e: HttpException) {
            throw e
        } as SuccessResult

        return DrawableResult(
            drawable = PlanespottersImage(
                result.drawable,
                PlanespottersMeta(
                    author = photo.photographer,
                    link = photo.link,
                ),
            ),
            isSampled = false,
            dataSource = result.dataSource,
        )
    }

    class Factory @Inject constructor(
        private val planespottersEndpoint: PlanespottersEndpoint,
    ) : Fetcher.Factory<Aircraft> {

        override fun create(
            aircraft: Aircraft,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher = PlanespottersFetcher(
            aircraft,
            options,
            imageLoader,
            planespottersEndpoint,
        )
    }

    companion object {
        private val TAG = logTag("Planespotters", "Fetcher")
    }
}
