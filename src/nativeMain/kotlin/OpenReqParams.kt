import kotlinx.serialization.Serializable

@Serializable
internal data class OpenReqParams(
    val point: String,
)