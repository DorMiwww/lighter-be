package com.dormiwww.di

import com.dormiwww.services.LightService
import com.dormiwww.security.loadSecretsConfig
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    val secrets = loadSecretsConfig(environment.config)
    val pingTimeout = environment.config.property("lighter.router.ping-timeout-seconds").getString().toInt()

    install(Koin) {
        slf4jLogger()
        modules(module {
            single { secrets }
            single { LightService(secrets.routerHost, pingTimeout) }
        })
    }
}
