import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class DoorOpenerService(
    private val tokenService: TokenService,
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

    suspend fun openDoor(doorphoneId: String, doorNumber: String): DoorResponseDto {
        println("Opening door: doorphone=$doorphoneId, door=$doorNumber")

        val token = tokenService.getValidIntercomToken()
        if (token == null) {
            return DoorResponseDto("error", "Failed to get valid token")
        }

        return try {
            val response = client.post("https://doorphone.app.evo73.ru/api/doorphone/$doorphoneId/open-door/$doorNumber") {
                headers {
                    append("Host", "doorphone.app.evo73.ru")
                    append("Connection", "keep-alive")
                    append("X-Device-UUID", deviceUuid)
                    append("Authorization", "Bearer $token")
                    append("Accept", "application/json, text/plain, */*")
                    append("User-Agent", "Mozilla/5.0 (Linux; Android 11; sdk_gphone_arm64 Build/RSR1.240422.006; wv) AppleWebKit/537.36")
                    append("Content-Type", "application/json")
                }
                setBody("{}")
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    println("Door opened successfully")
                    DoorResponseDto("success", "Door opened successfully")
                }
                HttpStatusCode.Unauthorized -> {
                    println("Token expired, refreshing and retrying...")
                    // Пытаемся обновить токены и повторить запрос
                    if (tokenService.refreshAuthToken()) {
                        val newToken = tokenService.getValidIntercomToken()
                        if (newToken != null) {
                            return retryOpenDoor(doorphoneId, doorNumber, newToken)
                        }
                    }
                    DoorResponseDto("error", "Authentication failed")
                }
                else -> {
                    println("Failed to open door: ${response.status}")
                    DoorResponseDto("error", "Failed to open door: ${response.status}")
                }
            }
        } catch (e: Exception) {
            println("Error opening door: ${e.message}")
            DoorResponseDto("error", "Network error: ${e.message}")
        }
    }

    private suspend fun retryOpenDoor(doorphoneId: String, doorNumber: String, token: String): DoorResponseDto {
        return try {
            val response = client.post("https://doorphone.app.evo73.ru/api/doorphone/$doorphoneId/open-door/$doorNumber") {
                headers {
                    append("Host", "doorphone.app.evo73.ru")
                    append("Connection", "keep-alive")
                    append("X-Device-UUID", deviceUuid)
                    append("Authorization", "Bearer $token")
                    append("Accept", "application/json, text/plain, */*")
                    append("User-Agent", "Mozilla/5.0 (Linux; Android 11; sdk_gphone_arm64 Build/RSR1.240422.006; wv) AppleWebKit/537.36")
                    append("Content-Type", "application/json")
                }
                setBody("{}")
            }

            if (response.status == HttpStatusCode.OK) {
                println("Door opened successfully on retry")
                DoorResponseDto("success", "Door opened successfully")
            } else {
                println("Failed to open door on retry: ${response.status}")
                DoorResponseDto("error", "Failed to open door on retry: ${response.status}")
            }
        } catch (e: Exception) {
            println("Error opening door on retry: ${e.message}")
            DoorResponseDto("error", "Network error on retry: ${e.message}")
        }
    }

    suspend fun close() {
        client.close()
    }
}
