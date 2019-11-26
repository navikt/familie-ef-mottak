package no.nav.familie.ef.mottak.integration

import no.nav.familie.ef.mottak.config.SakConfig
import no.nav.familie.ef.mottak.integration.dto.SøknadssakDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestOperations
import org.springframework.web.util.DefaultUriBuilderFactory

@Service
class SøknadClient(operations: RestOperations, sakConfig: SakConfig) : AbstractRestClient(operations) {

    private val sendInnUri = DefaultUriBuilderFactory().uriString(sakConfig.url).path(PATH_MOTTAK_DOKUMENT).build()

    private val log = LoggerFactory.getLogger(this::class.simpleName)

    fun sendTilSak(søknadDto: SøknadssakDto): ResponseEntity<HttpStatus> {
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
