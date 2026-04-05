package com.dormiwww.telegram

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File

@Serializable
data class LightState(
    val light: Boolean,
    val since: Long,
)

object StateStore {
    private val log = LoggerFactory.getLogger(StateStore::class.java)
    private val stateFile = File("data/light_state.json")

    fun load(): LightState? {
        if (!stateFile.exists()) return null
        return try {
            Json.decodeFromString<LightState>(stateFile.readText())
        } catch (e: Exception) {
            log.warn("Failed to read state file, starting fresh: ${e.message}")
            null
        }
    }

    fun save(state: LightState) {
        try {
            stateFile.parentFile.mkdirs()
            stateFile.writeText(Json.encodeToString(state))
        } catch (e: Exception) {
            log.error("Failed to save state: ${e.message}")
        }
    }
}
