package dev.yenny.calendar.auth.data.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Serializable(with = TokensApiResponse.Serializer::class)
internal sealed interface TokensApiResponse {

    @Serializable
    class Success(
        @SerialName("access_token")
        val accessToken: String,

        @SerialName("expires_in")
        val expiresIn: Int,

        // TODO https://developers.google.com/identity/protocols/oauth2#expiration
        @SerialName("refresh_token")
        val refreshToken: String,

        @SerialName("scope")
        val scope: String,

        @SerialName("token_type")
        val tokenType: String,

        @SerialName("id_token")
        val idToken: String,
    ) : TokensApiResponse

    @Serializable
    class Error(
        @SerialName("error")
        val error: String,

        @SerialName("error_description")
        val errorDescription: String?,
    ) : TokensApiResponse

    object Serializer :
        JsonContentPolymorphicSerializer<TokensApiResponse>(baseClass = TokensApiResponse::class) {

        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<TokensApiResponse> {
           return when {
                element.jsonObject["error"] != null -> Error.serializer()
                element.jsonObject["access_token"] != null -> Success.serializer()
                else -> error("Unknown response type.")
            }
        }
    }
}
