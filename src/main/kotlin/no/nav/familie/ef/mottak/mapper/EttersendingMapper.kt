package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_ETTERSENDING
import no.nav.familie.ef.mottak.repository.domain.EttersendingDb
import no.nav.familie.kontrakter.ef.søknad.Ettersending
import no.nav.familie.kontrakter.felles.objectMapper

object EttersendingMapper {

    fun fromDto(ettersending: Ettersending, behandleINySaksbehandling: Boolean): EttersendingDb {
        return EttersendingDb(ettersendingJson = objectMapper.writeValueAsString(ettersending),
                            fnr = ettersending.personalia.verdi.fødselsnummer.verdi.verdi,
                            dokumenttype = DOKUMENTTYPE_ETTERSENDING,
                            behandleINySaksbehandling = behandleINySaksbehandling
            )
    }
}