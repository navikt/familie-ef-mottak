package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.api.dto.SøknadDto
import no.nav.familie.ef.mottak.repository.domain.Søknad

interface SøknadService {

    fun motta(søknadDto: SøknadDto): Kvittering

    fun get(id: Long): Søknad

    fun sendTilSak(søknadId: String)
}
