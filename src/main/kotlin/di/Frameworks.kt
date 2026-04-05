package com.dormiwww.di

import com.dormiwww.security.loadSecretsConfig
import com.dormiwww.services.LightService
import com.dormiwww.telegram.TelegramNotifier
import io.ktor.server.application.*
import io.ktor.server.application.ApplicationStopped
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    val secrets = loadSecretsConfig(environment.config)
    val pingTimeout = environment.config.property("lighter.router.ping-timeout-seconds").getString().toInt()
    val lightService = LightService(secrets.routerHost, secrets.routerPort, pingTimeout)

    install(Koin) {
        slf4jLogger()
        modules(module {
            single { secrets }
            single { lightService }
        })
    }

    val token = secrets.telegramBotToken
    val chatId = secrets.telegramChatId?.toLongOrNull()
    if (token != null && chatId != null) {
        val notifier = TelegramNotifier(lightService, token, chatId)
        notifier.start(this)
        monitor.subscribe(ApplicationStopped) { notifier.stop() }
    }
}
