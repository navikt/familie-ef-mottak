package no.nav.familie.ef.mottak.integration

import no.nav.familie.ef.mottak.config.SakConfig
import no.nav.familie.ef.mottak.service.SøknadServiceImpl
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.ef.søknad.Søknad
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestOperations
import org.springframework.web.util.DefaultUriBuilderFactory

@Service
class SøknadClient(@Qualifier("restTemplateAzure") operations: RestOperations,
                   sakConfig: SakConfig) : AbstractRestClient(operations, "sak") {

    private val sendInnUri = DefaultUriBuilderFactory().uriString(sakConfig.url).path(PATH_MOTTAK_DOKUMENT).build()

    fun sendTilSak(søknadDto: Søknad): SøknadServiceImpl.OkDto? {
        log.info("Sender søknad til {}", sendInnUri)

        try {
            return postForEntity(sendInnUri, søknadDto)
        } catch (e: RestClientResponseException) {
            log.warn("Innsending til sak feilet. Responskode: {}, body: {}",
                     e.rawStatusCode,
                     e.responseBodyAsString)
            throw IllegalStateException("Innsending til sak feilet. Status: ${e.rawStatusCode}, body: ${e.responseBodyAsString}",
                                        e)
        } catch (e: RestClientException) {
            throw IllegalStateException("Innsending til sak feilet.", e)
        }

    }

    companion object {
        const val PATH_MOTTAK_DOKUMENT = "mottak/dokument"
    }

}
