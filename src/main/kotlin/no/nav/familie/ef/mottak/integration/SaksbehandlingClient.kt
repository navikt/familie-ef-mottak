package no.nav.familie.ef.mottak.integration

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Ressurs
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class SaksbehandlingClient(@Value("\${EF_SAK_URL}")
                           private val uri: URI,
                           @Qualifier("restTemplateAzure")
                           restOperations: RestOperations)
    : AbstractPingableRestClient(restOperations, "saksbehandling") {

    override val pingUri: URI = UriComponentsBuilder.fromUri(uri).pathSegment("api/ping").build().toUri()

    fun finnesBehandlingForPerson(stønadType: StønadType, personIdent: String): Boolean {
        val uri = UriComponentsBuilder.fromUri(uri)
                .pathSegment("/api/ekstern/behandling", stønadType.name, "finnes")
                .build().toUri()
        val response = postForEntity<Ressurs<Boolean>>(uri, PersonIdent(personIdent))
        val data = response.data
        if (data != null) {
            return data
        } else {
            error("Kall mot ef-sak feilet melding=${response.melding}")
        }
    }

}