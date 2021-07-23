package no.nav.familie.ef.mottak.api.dto

import java.time.LocalDateTime

data class EttersendingRequestData(
        val filnavn: String,
        val datoMottatt: LocalDateTime,
        val soknadId: String? = null,
        val dokumentasjonsbehovId: String? = null
)
