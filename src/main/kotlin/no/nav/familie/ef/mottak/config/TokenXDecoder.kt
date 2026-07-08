package no.nav.familie.ef.mottak.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtAudienceValidator
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Component

@Component
class TokenXDecoder(
    @Value("\${TOKEN_X_ISSUER}") private val tokenXIssuer: String,
    @Value("\${TOKEN_X_CLIENT_ID}") private val tokenXClientId: String,
) : JwtDecoder {
    companion object {
        const val LEVEL4 = "Level4"
        const val IDPORTEN_LOA_HIGH = "idporten-loa-high"
    }

    private val delegate by lazy {
        val decoder = NimbusJwtDecoder.withIssuerLocation(tokenXIssuer).build()
        decoder.setJwtValidator(
            DelegatingOAuth2TokenValidator(
                JwtValidators.createDefaultWithIssuer(tokenXIssuer),
                JwtAudienceValidator(tokenXClientId),
                JwtClaimValidator<String>("acr") { acr -> acr == LEVEL4 || acr == IDPORTEN_LOA_HIGH },
            ),
        )
        decoder
    }

    override fun decode(token: String): Jwt = delegate.decode(token)
}
