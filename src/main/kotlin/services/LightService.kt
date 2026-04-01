package com.dormiwww.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException

@Serializable
data class LightStatus(
    val light: Boolean,
    val checkedAt: Long
)

class LightService(
    private val routerHost: String,
    private val routerPort: Int,
    private val pingTimeoutSeconds: Int
) {
    @Volatile
    private var cached: LightStatus? = null

    suspend fun checkLight(): LightStatus {
        val now = System.currentTimeMillis() / 1000
        cached?.let { if (now - it.checkedAt < CACHE_TTL_SECONDS) return it }

        return withContext(Dispatchers.IO) {
            val reachable = checkRouter()
            LightStatus(light = reachable, checkedAt = now).also { cached = it }
        }
    }

    private fun checkRouter(): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(routerHost, routerPort), pingTimeoutSeconds * 1000)
            }
            true
        } catch (_: SocketException) {
            // Connection refused = router is reachable but no HTTP server
            true
        } catch (_: Exception) {
            // Timeout or other error = router not reachable
            false
        }
    }

    companion object {
        private const val CACHE_TTL_SECONDS = 10L
    }
}
