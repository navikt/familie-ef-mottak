package no.nav.familie.ef.mottak.integration

import no.nav.familie.ef.mottak.repository.domain.FeltMap
import no.nav.familie.ef.mottak.util.medContentTypeJsonUTF8
import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class PdfKvitteringClient(
    @Value("\${familie.pdf.url}")
    private val uri: URI,
    @Qualifier("restTemplateAzure")
    restOperations: RestOperations,
) : AbstractPingableRestClient(restOperations, "familie-pdf") {
    private val logger = LoggerFactory.getLogger(this::class.java)
    override val pingUri: URI =
        UriComponentsBuilder
            .fromUri(uri)
            .pathSegment("api/ping")
            .build()
            .toUri()

    fun opprettPdf(feltMap: FeltMap): ByteArray {
        val uri =
            UriComponentsBuilder
                .fromUri(uri)
                .pathSegment("api/v1/pdf/opprett-pdf")
                .build()
                .toUri()
        logger.info("Sender med body: ${objectMapper.writeValueAsString(feltMap)}")
        println(" Sender med body: ${objectMapper.writeValueAsString(feltMap)}")
        return postForEntity(uri, feltMap, HttpHeaders().medContentTypeJsonUTF8())
    }
}
