package no.nav.familie.ef.mottak.config

import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
class SpringSecurityTokenValidationContextHolder(
    @Value("\${AZURE_OPENID_CONFIG_ISSUER}") private val azureIssuer: String,
) : TokenValidationContextHolder {
    override fun getTokenValidationContext(): TokenValidationContext {
        val auth =
            SecurityContextHolder.getContext().authentication as? JwtAuthenticationToken
                ?: return TokenValidationContext(emptyMap())
        val issuer = auth.token.issuer?.toString()?.trimEnd('/') ?: ""
        val key = if (issuer == azureIssuer.trimEnd('/')) "azuread" else "tokenx"
        return TokenValidationContext(mapOf(key to JwtToken(auth.token.tokenValue)))
    }

    override fun setTokenValidationContext(tokenValidationContext: TokenValidationContext?) {
    }
}
