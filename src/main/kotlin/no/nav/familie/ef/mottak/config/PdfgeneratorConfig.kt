package no.nav.familie.ef.mottak.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("familie.ef.pdfgenerator")
@ConstructorBinding
data class PdfgeneratorConfig(val url: String)
