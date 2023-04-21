package no.nav.familie.ef.mottak.integration

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class FamilieDokumentClient(
    @Value("\${familie.dokument.url}")
    private val dokumentApiURI: URI,
    @Qualifier("tokenExchange") restTemplate: RestOperations,
) :
    AbstractPingableRestClient(restTemplate, "familie.dokument") {

    private val hentVedleggUri = UriComponentsBuilder.fromUri(dokumentApiURI).pathSegment(HENT).build().toUri()

    override val pingUri: URI = UriComponentsBuilder.fromUri(dokumentApiURI).pathSegment(PING).build().toUri()

    private fun vedleggUri(vedleggsId: String) =
        UriComponentsBuilder.fromUri(hentVedleggUri).path(vedleggsId).build().toUri()

    fun hentVedlegg(vedleggsId: String): ByteArray {
        val ressurs: Ressurs<ByteArray> = getForEntity(vedleggUri(vedleggsId))
        return ressurs.data ?: error("Ingen data på ressurs ved henting av vedlegg")
    }

    companion object {

        private const val HENT = "api/mapper/familievedlegg/"
        private const val PING = "api/mapper/ping"
    }
}
