package com.dormiwww.telegram

import com.dormiwww.services.LightService
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.TelegramFile
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TelegramNotifier(
    private val lightService: LightService,
    private val botToken: String,
    private val chatId: Long,
) {
    private val log = LoggerFactory.getLogger(TelegramNotifier::class.java)
    private val telegramBot = bot { token = botToken }

    @Volatile private var lastStatus: Boolean? = null
    @Volatile private var lastChangeTime: Long? = null
    private var job: Job? = null

    fun start(scope: CoroutineScope) {
        StateStore.load()?.let { saved ->
            lastStatus = saved.light
            lastChangeTime = saved.since
            log.info("Restored state: light=${saved.light}, since=${saved.since}")
        }

        job = scope.launch(Dispatchers.IO) {
            log.info("Telegram light notifier started (polling every ${POLL_INTERVAL_MS / 1000}s)")
            while (isActive) {
                try {
                    val status = lightService.checkLight()
                    if (status.light != lastStatus) {
                        log.info("Light status changed: $lastStatus -> ${status.light}")
                        val now = System.currentTimeMillis()
                        sendStatusImage(status.light, now)
                        lastStatus = status.light
                        lastChangeTime = now
                        StateStore.save(LightState(light = status.light, since = now))
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    log.error("Error in light notifier loop", e)
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stop() {
        job?.cancel()
        log.info("Telegram light notifier stopped")
    }

    private fun sendStatusImage(light: Boolean, now: Long) {
        val resourcePath = if (light) "/telegram/light_on.png" else "/telegram/light_off.png"
        val imageBytes = javaClass.getResourceAsStream(resourcePath)?.readBytes()
            ?: run {
                log.error("Image resource not found: $resourcePath")
                return
            }

        val caption = buildCaption(light, now)

        val (_, exception) = telegramBot.sendPhoto(
            chatId = ChatId.fromId(chatId),
            photo = TelegramFile.ByByteArray(imageBytes, if (light) "light_on.png" else "light_off.png"),
            caption = caption
        )
        if (exception != null) {
            log.error("Failed to send Telegram notification: ${exception.message}", exception)
        } else {
            log.info("Telegram notification sent: light=$light")
        }
    }

    private fun buildCaption(light: Boolean, now: Long): String {
        val zone = ZoneId.of("Europe/Kyiv")
        val fmt = DateTimeFormatter.ofPattern("HH:mm, dd.MM.yyyy")
        val nowFormatted = Instant.ofEpochMilli(now).atZone(zone).format(fmt)

        return buildString {
            if (light) appendLine("💡 Світло є!") else appendLine("🌑 Світла немає")
            appendLine("🕐 $nowFormatted")
            lastChangeTime?.let { changeTime ->
                val fromFormatted = Instant.ofEpochMilli(changeTime).atZone(zone).format(fmt)
                val durationMs = now - changeTime
                if (light) {
                    appendLine("📅 Світло не було з $fromFormatted до $nowFormatted")
                } else {
                    appendLine("📅 Світло було з $fromFormatted до $nowFormatted")
                }
                appendLine("⏱ ${formatDuration(durationMs)}")
            }
        }.trimEnd()
    }

    private fun formatDuration(ms: Long): String {
        val totalMinutes = ms / 60_000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 -> "$hours год $minutes хв"
            totalMinutes > 0 -> "$totalMinutes хв"
            else -> "менше хвилини"
        }
    }

    companion object {
        private const val POLL_INTERVAL_MS = 30_000L
    }
}
