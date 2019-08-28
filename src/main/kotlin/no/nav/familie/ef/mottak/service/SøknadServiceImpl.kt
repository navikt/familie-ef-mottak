package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.api.dto.Søknad
import no.nav.familie.ef.mottak.integration.SøknadClient
import no.nav.familie.ef.mottak.integration.dto.Kvittering
import org.springframework.stereotype.Service

@Service
class SøknadServiceImpl(private val søknadClient: SøknadClient) : SøknadService {

    override fun sendInn(søknad: Søknad): Kvittering {

        return søknadClient.sendInn(søknad)

    }
}