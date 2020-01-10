package no.nav.familie.ef.mottak.integration

import no.nav.familie.ef.mottak.config.SakConfig
import no.nav.familie.ef.mottak.integration.dto.SøknadssakDto
import no.nav.familie.ef.mottak.integration.rest.AbstractRestClient
import no.nav.familie.ef.mottak.service.SøknadServiceImpl
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestOperations
import org.springframework.web.util.DefaultUriBuilderFactory

@Service
class SøknadClient(operations: RestOperations, sakConfig: SakConfig) : AbstractRestClient(operations, "familie-ef-mottak->sak") {

    private val sendInnUri = DefaultUriBuilderFactory().uriString(sakConfig.url).path(PATH_MOTTAK_DOKUMENT).build()

    fun sendTilSak(søknadDto: SøknadssakDto): SøknadServiceImpl.OkDto? {
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
        private const val PATH_MOTTAK_DOKUMENT = "mottak/dokument"
    }

}
