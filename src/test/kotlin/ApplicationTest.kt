package com.dormiwww

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    private fun applicationTestConfig() = io.ktor.server.config.MapApplicationConfig(
        "lighter.router.host" to "127.0.0.1",
        "lighter.router.ping-timeout-seconds" to "1",
        "lighter.api-key" to "test-api-key"
    )

    @Test
    fun testRoot() = testApplication {
        environment { config = applicationTestConfig() }
        application { module() }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Lighter API", bodyAsText())
        }
    }

    @Test
    fun testLightInfoUnauthorized() = testApplication {
        environment { config = applicationTestConfig() }
        application { module() }
        client.get("/lightinfo").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun testLightInfoAuthorized() = testApplication {
        environment { config = applicationTestConfig() }
        application { module() }
        client.get("/lightinfo") {
            header(HttpHeaders.Authorization, "Bearer test-api-key")
            header(HttpHeaders.Accept, ContentType.Application.Json.toString())
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}
