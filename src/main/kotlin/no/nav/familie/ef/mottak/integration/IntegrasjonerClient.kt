package no.nav.familie.ef.mottak.integration


import no.nav.familie.ef.mottak.config.IntegrasjonerConfig
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Status
import no.nav.familie.kontrakter.felles.arkivering.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.arkivering.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.oppgave.OppgaveResponse
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgave
import no.nav.familie.log.NavHttpHeaders
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.DefaultUriBuilderFactory
import java.net.URI


@Service
class IntegrasjonerClient(@Qualifier("restTemplateAzure") operations: RestOperations,
                          private val integrasjonerConfig: IntegrasjonerConfig) :
        AbstractRestClient(operations, "Arkiv") {

    private val sendInnUri = DefaultUriBuilderFactory().uriString(integrasjonerConfig.url).path(PATH_SEND_INN).build()
    private val opprettOppgaveUri =
            DefaultUriBuilderFactory().uriString(integrasjonerConfig.url).path(PATH_OPPRETT_OPPGAVE).build()
    private val aktørUri = DefaultUriBuilderFactory().uriString(integrasjonerConfig.url).path(PATH_AKTØR).build()

    fun arkiver(arkiverDokumentRequest: ArkiverDokumentRequest): ArkiverDokumentResponse {
        val response =
                postForEntity<Ressurs<ArkiverDokumentResponse>>(sendInnUri, arkiverDokumentRequest)
        return response?.getDataOrThrow() ?: error("No respons data")
    }

    fun lagOppgave(opprettOppgave: OpprettOppgave): OppgaveResponse {
        val response =
                postForEntity<Ressurs<OppgaveResponse>>(opprettOppgaveUri, opprettOppgave)
        return response?.getDataOrThrow() ?: error("No respons data")
    }

    fun hentSaksnummer(journalPostId: String): String {
        return getForEntity(hentSaksnummerUri(journalPostId))
    }

    fun hentAktørId(personident: String): String {
        val response = postForEntity<List<String>>(aktørUri, personident)
        return response?.firstOrNull() ?: error("Kan ikke finne aktørId")
    }


    private fun hentSaksnummerUri(id: String): URI {
        return DefaultUriBuilderFactory()
                .uriString(integrasjonerConfig.url)
                .path(PATH_HENT_SAKSNUMMER)
                .queryParam("journalpostId", id)
                .build()
    }

    fun <T> Ressurs<T>.getDataOrThrow(): T {
        return when (this.status) {
            Status.SUKSESS -> data ?: error("Data er null i Ressurs")
            else -> error(melding)
        }
    }

    private fun HttpHeaders.medPersonident(personident: String): HttpHeaders {
        this.add(NavHttpHeaders.NAV_PERSONIDENT.asString(), personident)
        return this
    }

    companion object {
        const val PATH_SEND_INN = "arkiv/v2"
        const val PATH_HENT_SAKSNUMMER = "/journalpost/sak"
        const val PATH_OPPRETT_OPPGAVE = "/oppgave"
        const val PATH_AKTØR = "/personopplysning/aktorId/ENF"
    }

}
