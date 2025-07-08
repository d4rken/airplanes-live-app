package eu.darken.apl.main.ui.main

import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.apl.common.BuildConfigWrap
import eu.darken.apl.common.coroutine.DispatcherProvider
import eu.darken.apl.common.debug.logging.Logging.Priority.ERROR
import eu.darken.apl.common.debug.logging.asLog
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.github.GithubApi
import eu.darken.apl.common.github.GithubReleaseCheck
import eu.darken.apl.common.network.NetworkStateProvider
import eu.darken.apl.common.uix.ViewModel3
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import net.swiftzer.semver.SemVer
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    githubReleaseCheck: GithubReleaseCheck,
    networkStateProvider: NetworkStateProvider,
) : ViewModel3(
    dispatcherProvider = dispatcherProvider,
    tag = logTag("Main", "ViewModel")
) {

    val isInternetAvailable = networkStateProvider.networkState
        .map { it.isInternetAvailable }
        .asStateFlow()

    val newRelease = flow {
        val latestRelease = try {
            githubReleaseCheck.latestRelease("d4rken", "airplanes-live-app")
        } catch (e: Exception) {
            log(tag, ERROR) { "Release check failed: ${e.asLog()}" }
            null
        }
        emit(latestRelease)
    }
        .filterNotNull()
        .filter {
            val current = try {
                SemVer.parse(BuildConfigWrap.VERSION_NAME.removePrefix("v"))
            } catch (e: IllegalArgumentException) {
                log(tag, ERROR) { "Failed to parse current version: ${e.asLog()}" }
                return@filter false
            }
            log(tag) { "Current version is $current" }

            val latest = try {
                SemVer.parse(it.tagName.removePrefix("v"))
            } catch (e: IllegalArgumentException) {
                log(tag, ERROR) { "Failed to parse current version: ${e.asLog()}" }
                return@filter false
            }
            log(tag) { "Latest version is $latest" }
            current < latest
        }
        .asStateFlow()

    private fun findApkDownloadUrl(release: GithubApi.ReleaseInfo): String? {
        return release.assets.find { asset ->
            asset.name.endsWith(".apk", ignoreCase = true)
        }?.downloadUrl
    }

}