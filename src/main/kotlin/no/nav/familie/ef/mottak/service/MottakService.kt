package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.repository.domain.Henvendelse
import no.nav.familie.ef.mottak.repository.domain.Søknad

interface MottakService {

    fun motta(søknadDto: String): Kvittering

    //fun get(id: Long): Henvendelse

    fun get(id: Long): Søknad

}
