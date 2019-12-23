package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.api.dto.SøknadDto
import no.nav.familie.ef.mottak.api.dto.VedleggDto
import no.nav.familie.ef.mottak.integration.dto.SøknadssakDto
import no.nav.familie.ef.mottak.repository.domain.Fil
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg

object SøknadMapper {

    fun fromDto(soknad: Soknad): SøknadssakDto {
        requireNotNull(soknad.saksnummer, { "saksnummer er null" })
        requireNotNull(soknad.journalpostId, { "JournalpostId er null" })

        return SøknadssakDto(soknad.søknadJson,
                             soknad.saksnummer,
                             soknad.journalpostId)

    }

    fun fromDto(søknadDto: SøknadDto): Soknad {
        return Soknad(søknadJson = søknadDto.soknadJson,
                      søknadPdf = Fil(søknadDto.soknadPdf.toByteArray()),
                      fnr = søknadDto.fnr,
                      vedlegg = fromDto(søknadDto.vedlegg),
                      nySaksbehandling = søknadDto.nySaksbehandling)
    }

    private fun fromDto(vedlegg: List<VedleggDto>): List<Vedlegg> {
        return vedlegg.map { Vedlegg(data = Fil(it.data.toByteArray()), filnavn = (it.tittel)) }


    }

}