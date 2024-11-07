import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Response(
    @SerialName("status")
    val status: Status
) {
    enum class Status {
        @SerialName("ok")
        Ok,

        @SerialName("failed")
        Failed,
    }
}