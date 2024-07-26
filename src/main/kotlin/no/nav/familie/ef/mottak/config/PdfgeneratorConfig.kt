package no.nav.familie.ef.mottak.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("familie.ef.pdfgenerator")
data class PdfgeneratorConfig(
    val url: String,
)
