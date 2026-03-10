package no.nav.familie.ef.mottak.integration

import no.nav.familie.ef.mottak.config.FamilieBrevClientConfig
import no.nav.familie.ef.mottak.repository.domain.FeltMap
import no.nav.familie.ef.mottak.util.medContentTypeJsonUTF8
import no.nav.familie.restklient.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.DefaultUriBuilderFactory

@Service
class FamilieBrevClient(
    @Qualifier("restTemplateUnsecured") operations: RestOperations,
    private val familieBrevClientConfig: FamilieBrevClientConfig,
) : AbstractRestClient(operations, "pdf") {
    fun lagPdf(labelValueJson: FeltMap): ByteArray {
        val sendInnUri =
            DefaultUriBuilderFactory().uriString(familieBrevClientConfig.url).path("/api/generer-soknad").build()
        return postForEntity(sendInnUri, labelValueJson, HttpHeaders().medContentTypeJsonUTF8())
    }
}
