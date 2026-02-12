package com.dormiwww

import com.dormiwww.di.configureFrameworks
import com.dormiwww.routing.configureRouting
import com.dormiwww.security.configureSecurity
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureFrameworks()
    configureSecurity()
    configureRouting()
}
