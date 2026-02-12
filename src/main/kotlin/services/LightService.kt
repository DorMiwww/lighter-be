package com.dormiwww.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.util.concurrent.TimeUnit

@Serializable
data class LightStatus(
    val light: Boolean,
    val checkedAt: Long
)

class LightService(
    private val routerHost: String,
    private val pingTimeoutSeconds: Int
) {
    @Volatile
    private var cached: LightStatus? = null

    suspend fun checkLight(): LightStatus {
        val now = System.currentTimeMillis() / 1000
        cached?.let { if (now - it.checkedAt < CACHE_TTL_SECONDS) return it }

        return withContext(Dispatchers.IO) {
            val reachable = pingRouter()
            LightStatus(light = reachable, checkedAt = now).also { cached = it }
        }
    }

    private fun pingRouter(): Boolean {
        return try {
            val os = System.getProperty("os.name").lowercase()
            val command = when {
                "win" in os -> listOf("ping", "-n", "1", "-w", "${pingTimeoutSeconds * 1000}", routerHost)
                "mac" in os || "darwin" in os -> listOf("ping", "-c", "1", "-W", "${pingTimeoutSeconds * 1000}", routerHost)
                else -> listOf("ping", "-c", "1", "-W", "$pingTimeoutSeconds", routerHost)
            }
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
            val completed = process.waitFor(pingTimeoutSeconds.toLong() + 2, TimeUnit.SECONDS)
            if (!completed) {
                process.destroyForcibly()
                false
            } else {
                process.exitValue() == 0
            }
        } catch (_: Exception) {
            false
        }
    }

    companion object {
        private const val CACHE_TTL_SECONDS = 10L
    }
}
