package no.nav.familie.ef.mottak.integration

import no.nav.familie.kontrakter.ef.journalføring.AutomatiskJournalføringRequest
import no.nav.familie.kontrakter.ef.journalføring.AutomatiskJournalføringResponse
import no.nav.familie.kontrakter.ef.søknad.KanSendePåminnelseRequest
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.getDataOrThrow
import no.nav.familie.restklient.client.AbstractPingableRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class SaksbehandlingClient(
    @Value("\${EF_SAK_URL}")
    private val uri: URI,
    @Qualifier("restTemplateAzure")
    restOperations: RestOperations,
) : AbstractPingableRestClient(restOperations, "saksbehandling") {
    override val pingUri: URI =
        UriComponentsBuilder
            .fromUri(uri)
            .pathSegment("api/ping")
            .build()
            .toUri()

    fun kanOppretteFørstegangsbehandling(
        personIdent: String,
        stønadType: StønadType,
    ): Boolean {
        val kapOppretteUri =
            UriComponentsBuilder
                .fromUri(uri)
                .pathSegment("api")
                .pathSegment("ekstern")
                .pathSegment("automatisk-journalforing")
                .pathSegment("kan-opprette-behandling")
                .queryParam("type", stønadType.name)
                .encode()
                .build()
                .toUri()

        val response = postForEntity<Ressurs<Boolean>>(kapOppretteUri, PersonIdent(personIdent))
        return response.data ?: error("Kall mot ef-sak feilet melding=${response.melding}")
    }

    fun journalførAutomatisk(automatiskJournalføringRequest: AutomatiskJournalføringRequest): AutomatiskJournalføringResponse {
        val sendInnUri =
            UriComponentsBuilder
                .fromUri(uri)
                .pathSegment("api/ekstern/automatisk-journalforing/journalfor")
                .build()
                .toUri()

        val response =
            postForEntity<Ressurs<AutomatiskJournalføringResponse>>(sendInnUri, automatiskJournalføringRequest)
        return response.getDataOrThrow()
    }

    fun kanSendePåminnelseTilBruker(kanSendePåminnelseRequest: KanSendePåminnelseRequest): Boolean {
        val uri =
            UriComponentsBuilder
                .fromUri(uri)
                .pathSegment("api")
                .pathSegment("ekstern")
                .pathSegment("behandling")
                .pathSegment("kan-sende-påminnelse-til-bruker")
                .encode()
                .build()
                .toUri()

        val response = postForEntity<Ressurs<Boolean>>(uri, kanSendePåminnelseRequest)
        return response.data ?: error("Kall mot ef-sak feilet melding=${response.melding}")
    }
}
