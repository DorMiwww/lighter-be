package com.dormiwww.security

import io.ktor.server.application.*
import io.ktor.server.auth.*
import org.koin.ktor.ext.inject
import java.security.MessageDigest

fun Application.configureSecurity() {
    val secrets by inject<SecretsConfig>()

    authentication {
        bearer("api-key") {
            realm = "Lighter API"
            authenticate { credential ->
                val tokenBytes = credential.token.toByteArray(Charsets.UTF_8)
                val expectedBytes = secrets.apiKey.toByteArray(Charsets.UTF_8)
                if (MessageDigest.isEqual(tokenBytes, expectedBytes)) {
                    UserIdPrincipal("api-client")
                } else {
                    null
                }
            }
        }
    }
}
