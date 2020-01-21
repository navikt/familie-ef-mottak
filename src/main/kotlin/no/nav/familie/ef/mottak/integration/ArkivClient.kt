package no.nav.familie.ef.mottak.integration


import no.nav.familie.ef.mottak.config.IntegrasjonerConfig
import no.nav.familie.ef.mottak.integration.rest.AbstractRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Status
import no.nav.familie.kontrakter.felles.arkivering.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.arkivering.ArkiverDokumentResponse
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.DefaultUriBuilderFactory
import java.net.URI

fun <T> Ressurs<T>.getDataOrThrow(): T {
    return when (this.status) {
        Status.SUKSESS -> data ?: error("Data er null i Ressurs")
        else -> error(melding)
    }
}

@Service
class ArkivClient(operations: RestOperations,
                  private val integrasjonerConfig: IntegrasjonerConfig) :
        AbstractRestClient(operations, "Arkiv") {

    private val sendInnUri = DefaultUriBuilderFactory().uriString(integrasjonerConfig.url).path(PATH_SEND_INN).build()

    fun arkiver(arkiverDokumentRequest: ArkiverDokumentRequest): ArkiverDokumentResponse {
        val response =
                postForEntity<Ressurs<ArkiverDokumentResponse>>(sendInnUri, arkiverDokumentRequest)
        return response.getDataOrThrow()
    }

    fun hentSaksnummer(journalPostId: String): String {
        return getForEntity(hentSaksnummerUri(journalPostId))
    }

    private fun hentSaksnummerUri(id: String): URI {
        return DefaultUriBuilderFactory()
                .uriString(integrasjonerConfig.url)
                .path(PATH_HENT_SAKSNUMMER)
                .queryParam("journalpostId", id)
                .build()
    }

    companion object {
        const val PATH_SEND_INN = "arkiv/v2"
        private const val PATH_HENT_SAKSNUMMER = "/journalpost/sak"
    }

}
