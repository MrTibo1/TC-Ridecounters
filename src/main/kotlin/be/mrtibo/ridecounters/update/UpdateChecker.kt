package be.mrtibo.ridecounters.update

import be.mrtibo.ridecounters.Ridecounters.Companion.INSTANCE
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

object UpdateChecker {

    private const val API_URL = "https://hangar.papermc.io/api/v1/projects/tc-ridecounters/latestrelease"

    fun notify(old: String, new: String) {
        INSTANCE.logger.info("A new version of TC-Ridecounters is available! You are using version $old, latest is $new")
    }

    fun checkForUpdate() {
        val currentVersion: String = INSTANCE.pluginMeta.version
        HttpClient.newHttpClient().runCatching {
            use { client ->
                val version = client.send(
                    HttpRequest.newBuilder(URI.create(API_URL)).GET().header("accept", "text/plain").build(),
                    BodyHandlers.ofString()
                ).body()
                if (version != currentVersion && version.isNotBlank()) {
                    notify(currentVersion, version)
                }
            }
        }
    }
}