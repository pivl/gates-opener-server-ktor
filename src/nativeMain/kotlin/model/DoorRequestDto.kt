import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenDoorRequestDto(
    @SerialName("doorphone_id") val doorphoneId: String,
    @SerialName("door_number") val doorNumber: String
)

@Serializable
data class DoorResponseDto(
    @SerialName("status") val status: String,
    @SerialName("message") val message: String? = null
)
