package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.api.dto.Søknad
import no.nav.familie.ef.mottak.integration.dto.Kvittering

interface SøknadService {

    fun sendInn(søknad: Søknad): Kvittering

}