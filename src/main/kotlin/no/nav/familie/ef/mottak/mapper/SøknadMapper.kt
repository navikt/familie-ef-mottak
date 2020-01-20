package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.integration.dto.SøknadssakDto
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.søknad.Søknad
import no.nav.familie.kontrakter.felles.objectMapper

object SøknadMapper {

    fun fromDto(soknad: Soknad): SøknadssakDto {
        requireNotNull(soknad.saksnummer, { "saksnummer er null" })
        requireNotNull(soknad.journalpostId, { "JournalpostId er null" })

        return SøknadssakDto(soknad.søknadJson,
                             soknad.saksnummer,
                             soknad.journalpostId)
    }

    fun fromDto(søknad: Søknad): Soknad {
        return Soknad(søknadJson = objectMapper.writeValueAsString(søknad),
                      fnr = søknad.personalia.verdi.fødselsnummer.verdi.verdi)
    }

}