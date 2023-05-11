package no.nav.familie.ef.mottak.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

@ConfigurationProperties("familie.ef.integrasjoner")
data class IntegrasjonerConfig(val url: URI)
