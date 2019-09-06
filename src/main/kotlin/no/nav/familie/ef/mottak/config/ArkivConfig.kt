package no.nav.familie.ef.mottak.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

// TODO Endres til ConfigurationProperties ved release av SpringBoot 2.2.0
//@ConfigurationProperties(prefix = "familie.ef.sak", ignoreUnknownFields = false)
@Component
data class ArkivConfig(@Value("\${familie.ef.arkiv.url}") val url: String)