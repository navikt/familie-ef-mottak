package no.nav.familie.ef.mottak.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("familie.ef.brev")
data class FamilieBrevClientConfig(
    val url: String,
)
