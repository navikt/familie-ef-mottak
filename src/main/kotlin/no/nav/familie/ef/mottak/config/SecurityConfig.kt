package no.nav.familie.ef.mottak.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Value("\${AZURE_OPENID_CONFIG_ISSUER}") private val azureIssuer: String,
    private val tokenXDecoder: TokenXDecoder,
) {
    @Bean
    @Order(1)
    fun tokenXSecurityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .securityMatcher("/api/soknad/**", "/api/ettersending/**", "/api/person/**")
            .csrf { it.disable() }
            .authorizeHttpRequests { it.anyRequest().authenticated() }
            .oauth2ResourceServer { it.jwt { jwt -> jwt.decoder(tokenXDecoder) } }
            .build()

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    fun azureSecurityFilterChain(http: HttpSecurity): SecurityFilterChain =
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
            }.oauth2ResourceServer { it.jwt { jwt -> jwt.decoder(JwtDecoders.fromIssuerLocation(azureIssuer)) } }
            .httpBasic { it.disable() }
            .build()
}
