package dev.yenny.calendar.auth.data.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class GoogleCertificateResponse(

    @Contextual
    @SerialName("keys")
    val keys: List<Element>,
) {

    @Serializable
    class Element(

        @SerialName("n")
        val rsaModulus: String,

        @SerialName("e")
        val rsaPublicExponent: String,

        @SerialName("alg")
        val algorithm: String,

        @SerialName("kid")
        val keyId: String,

        @SerialName("kty")
        val keyType: String,

        @SerialName("use")
        val use: String,
    )
}
