package no.nav.familie.ef.mottak.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Value("\${AZURE_OPENID_CONFIG_ISSUER}") private val azureIssuer: String,
    @Value("\${TOKEN_X_ISSUER}") private val tokenxIssuer: String,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it
                    .requestMatchers(
                        "/internal/**",
                        "/actuator/**",
                        "/api/ping",
                        "/api/status/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                    ).permitAll()
                it.anyRequest().authenticated()
            }.oauth2ResourceServer {
                it.authenticationManagerResolver(
                    JwtIssuerAuthenticationManagerResolver.fromTrustedIssuers(azureIssuer, tokenxIssuer),
                )
            }.httpBasic { it.disable() }
            .build()
}
