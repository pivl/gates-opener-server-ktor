import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.headers
import io.ktor.http.*

internal class Opener(
    private val internalKey: String,
    private val baseUrl: String,
) {

    private val client = HttpClient(CIO)

    suspend fun open(point: String) {
        println("opening point: $point")
        val response = client.post(
            urlString = "$baseUrl/open/$internalKey/$point/"
        ) {
            headers {
                append("content-type", "application/json")
                append(HttpHeaders.Accept, "*/*")
            }
        }
        println("response status is ${response.status}")
    }
}