package com.dormiwww.routing

import com.dormiwww.services.LightService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val lightService by inject<LightService>()

    routing {
        get("/") {
            call.respondText("Lighter API")
        }
        authenticate("api-key") {
            get("/lightinfo") {
                call.respond(lightService.checkLight())
            }
        }
    }
}
