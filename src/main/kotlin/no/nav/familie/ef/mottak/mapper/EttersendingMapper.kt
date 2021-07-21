package no.nav.familie.ef.mottak.mapper

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_ETTERSENDING
import no.nav.familie.kontrakter.ef.ettersending.Ettersending as EttersendingDto
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.kontrakter.felles.objectMapper

object EttersendingMapper {

    inline fun <reified T : Any> toDto(ettersending: Ettersending): T {
        return objectMapper.readValue(ettersending.ettersendingJson)
    }

    fun fromDto(ettersending: EttersendingDto):Ettersending {
        return Ettersending(
                ettersendingJson = objectMapper.writeValueAsString(ettersending),
                fnr = ettersending.fnr,
                dokumenttype = DOKUMENTTYPE_ETTERSENDING,
        )
    }
}