package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.integration.dto.SøknadssakDto
import no.nav.familie.ef.mottak.repository.domain.Søknad

object SøknadssakMapper {

    fun toDto(søknad: Søknad): SøknadssakDto {
        requireNotNull(søknad.saksnummer, { "saksnummer er null" })
        requireNotNull(søknad.journalpostId, { "JournalpostId er null" })

        return SøknadssakDto(søknad.payload,
                             søknad.saksnummer,
                             søknad.journalpostId)

    }

}