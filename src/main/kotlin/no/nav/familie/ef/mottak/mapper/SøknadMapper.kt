package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.api.dto.SøknadDto
import no.nav.familie.ef.mottak.integration.dto.SøknadssakDto
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.repository.domain.Pdf
import no.nav.familie.ef.mottak.repository.domain.Vedlegg

object SøknadMapper {

    fun toDto(søknad: Søknad): SøknadssakDto {
        requireNotNull(søknad.saksnummer, { "saksnummer er null" })
        requireNotNull(søknad.journalpostId, { "JournalpostId er null" })

        return SøknadssakDto(søknad.søknadJson,
                             søknad.saksnummer,
                             søknad.journalpostId)

    }

    fun fromDto(søknadDto: SøknadDto): Søknad {
        return Søknad(søknadJson = søknadDto.soknad,
                      søknadPdf = Pdf(søknadDto.soknadPdf),
                      vedlegg = søknadDto.vedlegg.map { Vedlegg(data = it.data, filnavn = it.tittel) },
                      fnr = søknadDto.fnr)
    }

}