package com.dormiwww.security

import io.ktor.server.config.*
import org.yaml.snakeyaml.Yaml
import java.io.File

data class SecretsConfig(
    val apiKey: String,
    val routerHost: String
)

fun loadSecretsConfig(appConfig: ApplicationConfig): SecretsConfig {
    // 1. Ktor config (MapApplicationConfig in tests)
    val apiKeyFromConfig = appConfig.propertyOrNull("lighter.api-key")?.getString()
    val routerHostFromConfig = appConfig.propertyOrNull("lighter.router.host")?.getString()
    if (apiKeyFromConfig != null && apiKeyFromConfig.isNotBlank()) {
        return SecretsConfig(
            apiKey = apiKeyFromConfig,
            routerHost = routerHostFromConfig ?: "192.168.1.1"
        )
    }

    // 2. secrets.yaml file
    val secretsFile = File("secrets.yaml")
    if (secretsFile.exists()) {
        return parseSecretsYaml(secretsFile)
    }

    // 3. Environment variables
    val apiKey = System.getenv("LIGHTER_API_KEY")
        ?: error(
            "No secrets configured. Either:\n" +
                "  - Create secrets.yaml from secrets.yaml.example\n" +
                "  - Set LIGHTER_API_KEY environment variable"
        )
    val routerHost = System.getenv("LIGHTER_ROUTER_HOST") ?: "192.168.1.1"
    return SecretsConfig(apiKey = apiKey, routerHost = routerHost)
}

@Suppress("UNCHECKED_CAST")
private fun parseSecretsYaml(file: File): SecretsConfig {
    val data = Yaml().load<Map<String, Any>>(file.reader())
    val lighter = data["lighter"] as? Map<String, Any>
        ?: error("secrets.yaml: missing 'lighter' root key")
    val router = lighter["router"] as? Map<String, Any>

    val apiKey = lighter["api-key"] as? String
        ?: error("secrets.yaml: missing 'lighter.api-key'")
    require(apiKey.isNotBlank()) { "secrets.yaml: 'lighter.api-key' must not be blank" }

    val routerHost = (router?.get("host") as? String) ?: "192.168.1.1"
    return SecretsConfig(apiKey = apiKey, routerHost = routerHost)
}
