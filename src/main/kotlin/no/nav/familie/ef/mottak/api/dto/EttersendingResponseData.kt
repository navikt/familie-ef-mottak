package no.nav.familie.ef.mottak.api.dto

import no.nav.familie.kontrakter.ef.ettersending.EttersendingDto
import java.time.LocalDateTime

data class EttersendingResponseData(
        val ettersending: EttersendingDto,
        val mottattTidspunkt: LocalDateTime
)
