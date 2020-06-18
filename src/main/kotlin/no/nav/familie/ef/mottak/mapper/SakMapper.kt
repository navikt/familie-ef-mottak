package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.sak.SakRequest
import no.nav.familie.kontrakter.ef.sak.Skjemasak
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg

object SakMapper {

    fun toSkjemasak(soknad: Soknad): Skjemasak {
        return Skjemasak(SøknadMapper.toDto(soknad), soknad.saksnummer!!, soknad.journalpostId!!)
    }

    fun toSak(soknad: Soknad): SakRequest {
        return SakRequest(SøknadMedVedlegg(SøknadMapper.toDto(soknad), VedleggMapper.toDto(soknad)),
                          soknad.saksnummer!!,
                          soknad.journalpostId!!)
    }

}
