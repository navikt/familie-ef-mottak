package no.nav.familie.ef.mottak.integration

import no.nav.familie.ef.mottak.config.ArkivConfig
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.DefaultUriBuilderFactory

@Service
class ArkivClient(operations: RestOperations, arkivConfig: ArkivConfig) : AbstractRestClient(operations) {

    private val sendInnUri = DefaultUriBuilderFactory().uriString(arkivConfig.url).path(PATH_SEND_INN).build()

    fun arkiver(søknadDto: String): ResponseEntity<HttpStatus> {
        return ResponseEntity.ok(HttpStatus.CREATED)
//        return postForObject(sendInnUri, ObjectMapper().readValue(søknadDto))
    }

    companion object {
        private const val PATH_SEND_INN = "soknad/sendInn"
    }

}
