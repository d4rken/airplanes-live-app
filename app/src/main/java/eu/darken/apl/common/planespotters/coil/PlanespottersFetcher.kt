package eu.darken.apl.common.planespotters.coil

import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.ImageRequest
import coil.request.Options
import coil.request.SuccessResult
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
                options.context.getDrawable(R.drawable.aircraft_photo_unavailable)!!,
                PlanespottersMeta(
                    author = "?",
                    link = "https://www.planespotters.net",
                ),
            ),
            isSampled = false,
            dataSource = DataSource.MEMORY,
        )

        val request = ImageRequest.Builder(options.context).apply {
            // TODO Can we pick a better size if we know the target size
            data(photo.thumbnail.src)
            size(options.size)
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
