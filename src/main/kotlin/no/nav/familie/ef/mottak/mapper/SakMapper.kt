package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.sak.Sak
import no.nav.familie.kontrakter.ef.sak.Skjemasak

object SakMapper {

    fun toSkjemasak(soknad: Soknad): Skjemasak {
        return Skjemasak(SøknadMapper.toDto(soknad), soknad.saksnummer!!, soknad.journalpostId!!)
    }

    fun toSak(soknad: Soknad): Sak {
        return Sak(SøknadMapper.toDto(soknad), soknad.saksnummer!!, soknad.journalpostId!!)
    }

}
