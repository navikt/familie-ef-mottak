package no.nav.familie.ef.mottak.integration

import no.nav.familie.ef.mottak.util.medContentTypeJsonUTF8
import no.nav.familie.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.DefaultUriBuilderFactory

@Service
class ITextPdfClient(
    @Qualifier("restTemplateUnsecured") operations: RestOperations,
) : AbstractRestClient(operations, "pdf") {
    fun lagITextPdf(feltMap: Map<String, Any>): ByteArray {
        val sendInnUri = DefaultUriBuilderFactory().uriString("http://localhost:8083").path("api/generate-pdf").build()
        return postForEntity(sendInnUri, feltMap, HttpHeaders().medContentTypeJsonUTF8())
    }

    fun helsesjekk(): String {
        val helsesjekkUrl =
            DefaultUriBuilderFactory().uriString("http://localhost:8099").path("api/pdf/helsesjekk").build()
        val response: String =
            getForEntity(
                helsesjekkUrl,
                HttpHeaders().medContentTypeJsonUTF8(),
            )
        return response
    }
}
