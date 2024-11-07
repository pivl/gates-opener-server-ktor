import io.ktor.client.network.sockets.*
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

fun main(args: Array<String>) {
    val arguments = parseArgs(args)
    if (arguments.token == null || arguments.baseUrl == null || arguments.internalKey == null) {
        println(
            """
            Consider providing arguments:
            --token S - authentication token to use this API
            --base-url S - host that handles requests internally
            --internal-key S - key that is passed to the host
            Using default values:
            token = ${configuration.token}
            baseUrl = ${configuration.baseUrl}
            internalKey = ${configuration.internalKey}
        """.trimIndent()
        )
    }
    configuration = Configuration(
        token = arguments.token ?: configuration.token,
        baseUrl = arguments.baseUrl ?: configuration.baseUrl,
        internalKey = arguments.internalKey ?: configuration.internalKey,
    )
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

var configuration = Configuration(
    token = "9741ae57",
    baseUrl = "http://localhost:2186/api/opener",
    internalKey = "internal-key"
)

/**  possible args:
--token S - authentication token to use this API
--base-url S - host that handles requests internally
--internal-key S - key that is passed to the host
for example run app with arguments:
./gates-opener-server-ktor --token some-token --internal-key 34f093a1 --base-url http://somehost:port/
 */
fun parseArgs(args: Array<String>): Arguments {

    var token: String? = null
    var baseUrl: String? = null
    var internalKey: String? = null

    for (i in args.indices) {
        when (args[i]) {
            "--token" -> token = args.getOrNull(i + 1)
            "--base-url" -> baseUrl = args.getOrNull(i + 1)
            "--internal-key" -> internalKey = args.getOrNull(i + 1)
        }
    }

    return Arguments(
        token = token,
        baseUrl = baseUrl,
        internalKey = internalKey,
    )
}

fun Application.module() {

    val opener = Opener(
        internalKey = configuration.internalKey,
        baseUrl = configuration.baseUrl,
    )

    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        bearer {
            authenticate { bearerTokenCredential ->
                when (bearerTokenCredential.token) {
                    configuration.token -> bearerTokenCredential.token
                    else -> null
                }
            }
        }
    }

    routing {
        authenticate {
            post("/opener") {
                try {
                    val params = call.receive<OpenReqParams>()
                    opener.open(point = params.point)
                    call.respond(message = Response(Response.Status.Ok))
                } catch (e: ConnectTimeoutException) {
                    call.respondText(
                        status = HttpStatusCode.FailedDependency,
                        text = "",
                    )
                }
            }
        }
    }
}
