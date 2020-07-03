package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.sak.SakRequest
import no.nav.familie.kontrakter.ef.sak.Skjemasak
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import no.nav.familie.kontrakter.ef.søknad.Vedlegg

object SakMapper {

    fun toSkjemasak(soknad: Soknad): Skjemasak {
        return Skjemasak(SøknadMapper.toDto(soknad), soknad.saksnummer!!, soknad.journalpostId!!)
    }

    fun toSak(soknad: Soknad, vedlegg: List<Vedlegg>): SakRequest {
        return SakRequest(SøknadMedVedlegg(SøknadMapper.toDto(soknad), vedlegg),
                          soknad.saksnummer!!,
                          soknad.journalpostId!!)
    }

}
