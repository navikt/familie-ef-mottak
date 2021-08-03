package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.kontrakter.ef.ettersending.EttersendingDto
import no.nav.familie.kontrakter.felles.objectMapper

object EttersendingMapper {

    fun fromDto(ettersending: EttersendingDto):Ettersending {
        return Ettersending(
                ettersendingJson = objectMapper.writeValueAsString(ettersending),
                fnr = ettersending.fnr,
                stønadType = ettersending.stønadType.toString(),
        )
    }
}