package no.nav.familie.ef.mottak.integration

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

@ConfigurationProperties("familie.pdf")
class PdfKvitteringConfig(
    val url: URI,
)
