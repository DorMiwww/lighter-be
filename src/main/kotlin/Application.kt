package com.dormiwww

import com.dormiwww.di.configureFrameworks
import com.dormiwww.routing.configureRouting
import com.dormiwww.security.configureSecurity
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) { json() }
    configureFrameworks()
    configureSecurity()
    configureRouting()
}
