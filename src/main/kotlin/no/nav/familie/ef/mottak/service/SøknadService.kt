package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.api.dto.SøknadDto
import no.nav.familie.ef.mottak.repository.domain.Soknad

interface SøknadService {

    fun motta(søknadDto: SøknadDto): Kvittering

    fun get(id: String): Soknad

    fun sendTilSak(søknadId: String)

    fun lagreSøknad(soknad: Soknad)
}
