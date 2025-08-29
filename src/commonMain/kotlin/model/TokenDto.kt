import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    @SerialName("statusCode") val statusCode: Int,
    @SerialName("data") val data: AuthDataDto
)

@Serializable
data class AuthDataDto(
    @SerialName("status") val status: String,
    @SerialName("token") val token: String
)

@Serializable
data class SingleAuthTokenDto(
    @SerialName("token") val token: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Int
)

@Serializable
data class IntercomTokenDto(
    @SerialName("token") val token: String
)

@Serializable
data class TokenRequestDto(
    @SerialName("token") val token: String
)

@Serializable
data class RefreshTokenResponseDto(
    @SerialName("statusCode") val statusCode: Int,
    @SerialName("data") val data: AuthDataDto
)
