package no.nav.familie.ef.mottak.config

import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties("no.nav.security.jwt.client")
@ConstructorBinding
class ClientConfigurationPropertiesLocal(val registration: Map<String, ClientPropertiesMedUrl>)
