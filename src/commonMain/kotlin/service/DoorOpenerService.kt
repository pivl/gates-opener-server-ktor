import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class DoorOpenerService(
    private val httpClient: HttpClient,
    private val tokenService: TokenService,
    private val deviceUuid: String,
) {
    suspend fun openDoor(doorphoneId: String, doorNumber: String): DoorResponseDto {
        println("🚪 DoorOpenerService: Opening door: doorphone=$doorphoneId, door=$doorNumber")

        val token = tokenService.getValidIntercomToken()
        if (token == null) {
            println("❌ DoorOpenerService: Failed to get valid token")
            return DoorResponseDto("error", "Failed to get valid token")
        }
        println("🔑 DoorOpenerService: Got valid token, proceeding with door opening")

        return try {
            println("🔗 DoorOpenerService: Making request to https://doorphone.app.evo73.ru/api/doorphone/$doorphoneId/open-door/$doorNumber")
            val response = httpClient.post("https://doorphone.app.evo73.ru/api/doorphone/$doorphoneId/open-door/$doorNumber") {
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

            println("📡 DoorOpenerService: Received response with status: ${response.status}")
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ DoorOpenerService: Door opened successfully")
                    DoorResponseDto("success", "Door opened successfully")
                }
                HttpStatusCode.Unauthorized -> {
                    println("🔄 DoorOpenerService: Token expired (401), refreshing and retrying...")
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
                    println("❌ DoorOpenerService: Failed to open door: ${response.status}")
                    println("📄 DoorOpenerService: Response body: ${response.bodyAsText()}")
                    DoorResponseDto("error", "Failed to open door: ${response.status}")
                }
            }
        } catch (e: Exception) {
            println("❌ DoorOpenerService: Error opening door: ${e.message}")
            e.printStackTrace()
            DoorResponseDto("error", "Network error: ${e.message}")
        }
    }

    private suspend fun retryOpenDoor(doorphoneId: String, doorNumber: String, token: String): DoorResponseDto {
        println("🔄 DoorOpenerService: Retrying door opening with new token")
        return try {
            val response = httpClient.post("https://doorphone.app.evo73.ru/api/doorphone/$doorphoneId/open-door/$doorNumber") {
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
                println("✅ DoorOpenerService: Door opened successfully on retry")
                DoorResponseDto("success", "Door opened successfully")
            } else {
                println("❌ DoorOpenerService: Failed to open door on retry: ${response.status}")
                println("📄 DoorOpenerService: Retry response body: ${response.bodyAsText()}")
                DoorResponseDto("error", "Failed to open door on retry: ${response.status}")
            }
        } catch (e: Exception) {
            println("❌ DoorOpenerService: Error opening door on retry: ${e.message}")
            e.printStackTrace()
            DoorResponseDto("error", "Network error on retry: ${e.message}")
        }
    }

    suspend fun close() {
        httpClient.close()
    }
}