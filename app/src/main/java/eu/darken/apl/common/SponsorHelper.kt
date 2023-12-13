package eu.darken.apl.common

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SponsorHelper @Inject constructor(
    private val webpageTool: WebpageTool,
) {

    suspend fun openSponsorPage() {
        webpageTool.open("https://github.com/sponsors/d4rken")
    }
}