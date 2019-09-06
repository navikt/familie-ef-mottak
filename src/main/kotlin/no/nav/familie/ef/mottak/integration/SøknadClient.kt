package no.nav.familie.ef.mottak.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.config.SakConfig
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.DefaultUriBuilderFactory

@Service
class SøknadClient(operations: RestOperations, sakConfig: SakConfig) : AbstractRestClient(operations) {

    private val sendInnUri = DefaultUriBuilderFactory().uriString(sakConfig.url).path(PATH_MOTTAK_DOKUMENT).build()

    fun sendTilSak(søknadDto: String): ResponseEntity<HttpStatus> {
        return postForEntity(sendInnUri, ObjectMapper().readValue(søknadDto))
    }

    companion object {
        private const val PATH_MOTTAK_DOKUMENT = "mottak/dokument"
    }

}