import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
    val arguments = parseArgs(args)
    if (arguments.apiToken == null || arguments.initialToken == null) {
        println(
            """
            Consider providing arguments:
            --api-token S - authentication token for API access (used by external clients)
            --initial-token S - initial token for obtaining system tokens
            --device-uuid S - device UUID (optional, defaults to fe9883696cbcffff)
        """.trimIndent()
        )
    }
    configuration = Configuration(
        apiToken = arguments.apiToken ?: configuration.apiToken,
        initialToken = arguments.initialToken ?: configuration.initialToken,
        deviceUuid = arguments.deviceUuid ?: configuration.deviceUuid,
    )
    println("""
Using this configuration:
  apiToken = ${configuration.apiToken}
  initialToken = ${configuration.initialToken}
  deviceUuid = ${configuration.deviceUuid}
""")
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

var configuration = Configuration(
    apiToken = "default-api-token",
    initialToken = "default-initial-token"
)

/**  possible args:
--api-token S - authentication token for API access (used by external clients)
--initial-token S - initial token for obtaining system tokens (former internal-key)
--device-uuid S - device UUID (optional)
for example run app with arguments:
./gates-opener-server-ktor --api-token some-api-token --initial-token eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9... --device-uuid fe9883696cbcffff
 */
fun parseArgs(args: Array<String>): Arguments {

    var apiToken: String? = null
    var initialToken: String? = null
    var deviceUuid: String? = null

    for (i in args.indices) {
        when (args[i]) {
            "--api-token" -> apiToken = args.getOrNull(i + 1)
            "--initial-token" -> initialToken = args.getOrNull(i + 1)
            "--device-uuid" -> deviceUuid = args.getOrNull(i + 1)
        }
    }

    return Arguments(
        apiToken = apiToken,
        initialToken = initialToken,
        deviceUuid = deviceUuid,
    )
}

fun Application.module() {
    
    // Инициализируем сервисы
    val tokenService = TokenService(
        initialToken = configuration.initialToken,
        deviceUuid = configuration.deviceUuid
    )
    
    val doorOpenerService = DoorOpenerService(
        tokenService = tokenService,
        deviceUuid = configuration.deviceUuid
    )

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }

    install(Authentication) {
        bearer {
            authenticate { bearerTokenCredential ->
                println("🔐 Authentication attempt with token: ${bearerTokenCredential.token}")
                println("🔑 Expected token: ${configuration.apiToken}")
                when (bearerTokenCredential.token) {
                    configuration.apiToken -> {
                        println("✅ Authentication successful")
                        bearerTokenCredential.token
                    }
                    else -> {
                        println("❌ Authentication failed - token mismatch")
                        null
                    }
                }
            }
        }
    }

    routing {
        authenticate {
            post("/open-door") {
                println("🔄 Received POST /open-door request")
                println("📝 Headers: ${call.request.headers.entries()}")
                try {
                    val request = call.receive<OpenDoorRequestDto>()
                    println("📦 Request body: doorphone_id=${request.doorphoneId}, door_number=${request.doorNumber}")
                    
                    val result = doorOpenerService.openDoor(
                        doorphoneId = request.doorphoneId,
                        doorNumber = request.doorNumber
                    )
                    println("✅ Door opener result: ${result.status} - ${result.message}")
                    call.respond(message = result)
                } catch (e: Exception) {
                    println("❌ Error in /open-door endpoint: ${e.message}")
                    e.printStackTrace()
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        message = DoorResponseDto("error", "Internal server error: ${e.message}")
                    )
                }
            }
        }
        
        // Простой эндпоинт для проверки состояния
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }
    }
    
    // Cleanup при выключении приложения
    environment.monitor.subscribe(ApplicationStopped) {
        runBlocking {
            tokenService.close()
            doorOpenerService.close()
        }
    }
}
