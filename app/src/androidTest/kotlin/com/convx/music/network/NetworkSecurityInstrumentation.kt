package com.convx.music.network

import android.app.Activity
import android.app.Instrumentation
import android.os.Bundle
import android.security.NetworkSecurityPolicy
import com.convx.music.artistvideo.ArtistVideoCanvasProvider
import com.convx.music.canvas.AppleMusicCatalog
import com.music.lrclib.LrcLib
import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/** Runs against the installed debug app, whose Android trust policy JVM tests cannot exercise. */
class NetworkSecurityInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) {
        super.onCreate(arguments)
        start()
    }

    override fun onStart() {
        val report = StringBuilder()
        try {
            val policy = NetworkSecurityPolicy.getInstance()
            check(policy.isCleartextTrafficPermitted("localhost"))
            runBlocking {
                val targets = listOf(
                    Triple(LrcLib, "getClient", "https://lrclib.net/"),
                    Triple(AppleMusicCatalog, "getClient", "https://amp-api.music.apple.com/"),
                    Triple(ArtistVideoCanvasProvider, "getClient", "https://www.apple.com/"),
                )
                for ((provider, getter, url) in targets) {
                    val method = provider.javaClass.declaredMethods.first {
                        it.name == getter || it.name.startsWith(getter + "$")
                    }.apply { isAccessible = true }
                    val client = method.invoke(provider) as HttpClient
                    check(client.engine.javaClass.simpleName == "OkHttpEngine") {
                        "${provider.javaClass.simpleName} must use Android-aware TLS"
                    }
                    withTimeout(20_000) {
                        val response = client.get(url) { expectSuccess = false }
                        // Any HTTP response, including an unauthenticated 401/404, proves TLS completed.
                        response.bodyAsText()
                        report.appendLine("${provider.javaClass.simpleName}: HTTP ${response.status.value}")
                    }
                }
            }
            finish(Activity.RESULT_OK, Bundle().apply { putString("stream", report.toString()) })
        } catch (failure: Exception) {
            finish(Activity.RESULT_CANCELED, Bundle().apply {
                putString("stream", report.toString() + "FAILED: ${failure.javaClass.simpleName}: ${failure.message}")
            })
        }
    }
}
