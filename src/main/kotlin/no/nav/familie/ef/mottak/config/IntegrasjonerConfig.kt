package no.nav.familie.ef.mottak.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("familie.ef.integrasjoner")
@ConstructorBinding
data class IntegrasjonerConfig(val url: String)
