import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class TokenService(
    private val httpClient: HttpClient,
    private val initialToken: String,
    private val deviceUuid: String = "fe9883696cbc9018"
) {
    private val mutex = Mutex()
    private var authToken: String = initialToken
    private var mainToken: String? = null
    private var intercomToken: String? = null

    // Обновление первичного токена через refresh
    suspend fun refreshAuthToken(): Boolean = mutex.withLock {
        return try {
            println("🔄 TokenService: Refreshing auth token...")
            println("🔗 TokenService: Making request to https://id.evo73.ru/auth/refresh")
            val response = httpClient.post("https://id.evo73.ru/auth/refresh") {
                headers {
                    append("X-Device-UUID", deviceUuid)
                    append("Authorization", "Bearer $authToken")
                    append("Accept", "application/json, text/plain, */*")
                    append("Content-Type", "application/json")
                }
                setBody("{}")
            }

            if (response.status == HttpStatusCode.OK) {
                val refreshResponse = response.body<RefreshTokenResponseDto>()
                authToken = refreshResponse.data.token
                // После обновления auth токена сбрасываем зависимые токены
                mainToken = null
                intercomToken = null
                println("✅ TokenService: Auth token refreshed successfully")
                true
            } else {
                println("❌ TokenService: Failed to refresh auth token: ${response.status}")
                println("📄 TokenService: Response body: ${response.bodyAsText()}")
                false
            }
        } catch (e: Exception) {
            println("❌ TokenService: Error refreshing auth token: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    // Получение main токена
    suspend fun getMainToken(): String? = mutex.withLock {
        if (mainToken != null) {
            println("🔄 TokenService: Using cached main token")
            return mainToken
        }

        return try {
            println("🔄 TokenService: Getting main token...")
            println("🔗 TokenService: Making request to https://api.app.evo73.ru/api/v2/single-auth/main-token")
            val response = httpClient.post("https://api.app.evo73.ru/api/v2/single-auth/main-token") {
                headers {
                    append("X-Device-UUID", deviceUuid)
                    append("Accept", "application/json, text/plain, */*")
                    append("Content-Type", "application/json")
                }
                setBody(TokenRequestDto(authToken))
            }

            if (response.status == HttpStatusCode.OK) {
                val tokenResponse = response.body<SingleAuthTokenDto>()
                mainToken = tokenResponse.token
                println("✅ TokenService: Main token obtained successfully")
                mainToken
            } else {
                println("❌ TokenService: Failed to get main token: ${response.status}")
                println("📄 TokenService: Response body: ${response.bodyAsText()}")
                null
            }
        } catch (e: Exception) {
            println("❌ TokenService: Error getting main token: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // Получение intercom токена
    suspend fun getIntercomToken(): String? {
        // Сначала проверяем кеш под блокировкой
        mutex.withLock {
            if (intercomToken != null) {
                println("🔄 TokenService: Using cached intercom token")
                return intercomToken
            }
        }

        // Получаем main token вне блокировки, чтобы избежать дедлока
        val currentMainToken = mutex.withLock { mainToken } ?: getMainToken()
        if (currentMainToken == null) {
            println("❌ TokenService: Cannot get intercom token: main token is null")
            return null
        }

        return try {
            println("🔄 TokenService: Getting intercom token...")
            println("🔗 TokenService: Making request to https://api.app.evo73.ru/api/v1/authIntercom")
            val response = httpClient.post("https://api.app.evo73.ru/api/v1/authIntercom") {
                headers {
                    append("X-Device-UUID", deviceUuid)
                    append("Authorization", "Bearer $currentMainToken")
                    append("Accept", "application/json, text/plain, */*")
                    append("Content-Type", "application/json")
                }
                setBody("{}")
            }

            if (response.status == HttpStatusCode.OK) {
                val tokenResponse = response.body<IntercomTokenDto>()
                // Сохраняем токен под блокировкой
                mutex.withLock {
                    intercomToken = tokenResponse.token
                }
                println("✅ TokenService: Intercom token obtained successfully")
                tokenResponse.token
            } else {
                println("❌ TokenService: Failed to get intercom token: ${response.status}")
                println("📄 TokenService: Response body: ${response.bodyAsText()}")
                null
            }
        } catch (e: Exception) {
            println("❌ TokenService: Error getting intercom token: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // Автоматическое обновление токенов при получении 401
    suspend fun getValidIntercomToken(): String? {
        println("🔄 TokenService: Getting valid intercom token...")
        var token = getIntercomToken()
        if (token == null) {
            println("⚠️ TokenService: Intercom token is null, trying to refresh auth token...")
            // Попытка обновить auth токен и получить новые токены
            if (refreshAuthToken()) {
                println("🔄 TokenService: Auth token refreshed, getting new intercom token...")
                token = getIntercomToken()
            } else {
                println("❌ TokenService: Failed to refresh auth token")
            }
        }
        println("🎯 TokenService: Returning intercom token: ${if (token != null) "✅ Valid" else "❌ Null"}")
        return token
    }

    suspend fun close() {
        httpClient.close()
    }
}