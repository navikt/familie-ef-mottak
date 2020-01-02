package no.nav.familie.ef.mottak.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties("no.nav.security.jwt.client")
@ConstructorBinding
class ClientConfigurationPropertiesLocal(val registration: Map<String, ClientPropertiesMedUrl>)
