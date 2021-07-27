package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_ETTERSENDING
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.ef.ettersending.EttersendingDto

object EttersendingMapper {

    fun fromDto(ettersending: EttersendingDto):Ettersending {
        return Ettersending(
                ettersendingJson = objectMapper.writeValueAsString(ettersending),
                fnr = ettersending.fnr,
                dokumenttype = DOKUMENTTYPE_ETTERSENDING,
        )
    }
}