package no.nav.familie.ef.mottak.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

@Validated
@ConfigurationProperties("no.nav.security.jwt.client")
@ConstructorBinding
class ClientConfigurationProperties(val registration: @NotEmpty @Valid Map<String, ClientPropertiesMedUrl>)
