import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class TokenService(
    private val initialToken: String,
    private val deviceUuid: String = "fe9883696cbc9018"
) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val mutex = Mutex()
    private var authToken: String = initialToken
    private var mainToken: String? = null
    private var intercomToken: String? = null

    // Обновление первичного токена через refresh
    suspend fun refreshAuthToken(): Boolean = mutex.withLock {
        return try {
            println("Refreshing auth token...")
            val response = client.post("https://id.evo73.ru/auth/refresh") {
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
                println("Auth token refreshed successfully")
                true
            } else {
                println("Failed to refresh auth token: ${response.status}")
                false
            }
        } catch (e: Exception) {
            println("Error refreshing auth token: ${e.message}")
            false
        }
    }

    // Получение main токена
    suspend fun getMainToken(): String? = mutex.withLock {
        if (mainToken != null) return mainToken

        return try {
            println("Getting main token...")
            val response = client.post("https://api.app.evo73.ru/api/v2/single-auth/main-token") {
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
                println("Main token obtained successfully")
                mainToken
            } else {
                println("Failed to get main token: ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("Error getting main token: ${e.message}")
            null
        }
    }

    // Получение intercom токена
    suspend fun getIntercomToken(): String? = mutex.withLock {
        if (intercomToken != null) return intercomToken

        val currentMainToken = mainToken ?: getMainToken()
        if (currentMainToken == null) {
            println("Cannot get intercom token: main token is null")
            return null
        }

        return try {
            println("Getting intercom token...")
            val response = client.post("https://api.app.evo73.ru/api/v1/authIntercom") {
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
                intercomToken = tokenResponse.token
                println("Intercom token obtained successfully")
                intercomToken
            } else {
                println("Failed to get intercom token: ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("Error getting intercom token: ${e.message}")
            null
        }
    }

    // Автоматическое обновление токенов при получении 401
    suspend fun getValidIntercomToken(): String? {
        var token = getIntercomToken()
        if (token == null) {
            // Попытка обновить auth токен и получить новые токены
            if (refreshAuthToken()) {
                token = getIntercomToken()
            }
        }
        return token
    }

    suspend fun close() {
        client.close()
    }
}
