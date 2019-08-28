package no.nav.familie.ef.mottak.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URI

// TODO Endres til ConfigurationProperties ved release av SpringBoot 2.2.0
//@ConfigurationProperties(prefix = "søknad", ignoreUnknownFields = false)
@Component
data class SakConfig(@Value("\${familie.ef.mottak.url}") val Uri: URI)