import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main(args: Array<String>) {
    val configuration = initializeConfiguration(args)
    embeddedServer(CIO, port = 8080, host = "0.0.0.0") { module(configuration) }
        .start(wait = true)
}
