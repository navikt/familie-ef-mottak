package no.nav.familie.ef.mottak.config

import no.nav.familie.sikkerhet.context.TokenContext
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class SpringSecurityTokenContext(
    @Value("\${AZURE_OPENID_CONFIG_ISSUER}") private val azureIssuer: String,
) : TokenContext,
    TokenValidationContextHolder {
    private fun jwt(): JwtAuthenticationToken? = SecurityContextHolder.getContext().authentication as? JwtAuthenticationToken

    private fun issuerKey(): String {
        val issuer =
            jwt()
                ?.token
                ?.issuer
                ?.toString()
                ?.trimEnd('/') ?: ""
        return if (issuer == azureIssuer.trimEnd('/')) "azuread" else "tokenx"
    }

    override fun getClaimAsString(
        claim: String,
        issuer: String,
    ): String? = if (hasTokenFor(issuer)) jwt()?.token?.getClaimAsString(claim) else null

    override fun getClaimAsStringList(
        claim: String,
        issuer: String,
    ): List<String>? = if (hasTokenFor(issuer)) jwt()?.token?.getClaimAsStringList(claim) else null

    override fun hasTokenFor(issuer: String): Boolean = jwt() != null && issuerKey() == issuer

    override fun getBearerToken(issuer: String): String? = if (hasTokenFor(issuer)) jwt()?.token?.tokenValue else null

    override fun issuers(): Collection<String> = if (jwt() != null) listOf(issuerKey()) else emptyList()

    override fun getExpiry(issuer: String): Instant? = if (hasTokenFor(issuer)) jwt()?.token?.expiresAt else null

    override fun getTokenValidationContext(): TokenValidationContext {
        val auth = jwt() ?: return TokenValidationContext(emptyMap())
        return TokenValidationContext(mapOf(issuerKey() to JwtToken(auth.token.tokenValue)))
    }

    override fun setTokenValidationContext(tokenValidationContext: TokenValidationContext?) {}
}
