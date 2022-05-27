package no.nav.familie.ef.mottak.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.net.URL

@ConfigurationProperties("ettersending")
@ConstructorBinding
data class EttersendingConfig(val ettersendingUrl: URL)
