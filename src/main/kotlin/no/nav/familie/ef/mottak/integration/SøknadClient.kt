package no.nav.familie.ef.mottak.integration

import no.nav.familie.ef.mottak.api.dto.Søknad
import no.nav.familie.ef.mottak.config.SakConfig
import no.nav.familie.ef.mottak.integration.dto.Kvittering
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations

@Service
class SøknadClient(operations: RestOperations,
                   val sakConfig: SakConfig) : AbstractRestClient(operations) {

    override val isEnabled: Boolean = true


    fun sendInn(søknad: Søknad): Kvittering {


        return Kvittering("""bekreftelse fra mottak på innsending av "${søknad.text}" """)
//        return postForObject(søknadConfig.Uri, søknad)
    }

}