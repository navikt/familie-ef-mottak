package no.nav.familie.ef.mottak.integration

import no.nav.familie.ef.mottak.config.ArkivConfig
import no.nav.familie.ef.mottak.config.ClientConfigurationPropertiesLocal
import no.nav.familie.ef.mottak.integration.dto.ArkiverDokumentResponse
import no.nav.familie.ef.mottak.integration.dto.ArkiverSøknadRequest
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestOperations
import org.springframework.web.util.DefaultUriBuilderFactory
import java.net.URI

@Service
class ArkivClient(operations: RestOperations,
                  private val arkivConfig: ArkivConfig,
                  private val oAuth2AccessTokenService: OAuth2AccessTokenService,
                  private val clientConfigurationPropertiesLocal: ClientConfigurationPropertiesLocal) : AbstractRestClient(
        operations) {

    private val sendInnUri = DefaultUriBuilderFactory().uriString(arkivConfig.url).path(PATH_SEND_INN).build()

    fun arkiver(arkiverSøknadRequest: ArkiverSøknadRequest): ArkiverDokumentResponse {
        try {
            return postForEntity(sendInnUri, arkiverSøknadRequest)
        } catch (e: RestClientResponseException) {
            logger.warn("Innsending til dokarkiv feilet. Responskode: {}, body: {}",
                        e.rawStatusCode,
                        e.responseBodyAsString)
            throw IllegalStateException("Innsending til dokarkiv feilet. Status: ${e.rawStatusCode}, " +
                                        "body: ${e.responseBodyAsString}",
                                        e)
        } catch (e: RestClientException) {
            throw IllegalStateException("Innsending til dokarkiv feilet.", e)
        }
    }

    fun hentSaksnummer(journalPostId: String): String {

        return getForEntity(hentSaksnummerUri(journalPostId))
    }

    fun hentJournalpostId(callId: String): String {
        val uri = hentJournalpostIdUri(callId)
        return getForEntity(uri)
    }

    private fun hentSaksnummerUri(id: String): URI {
        return DefaultUriBuilderFactory()
                .uriString(arkivConfig.url)
                .path(PATH_HENT_SAKSNUMMER.format(id))
                .build()
    }

    private fun hentJournalpostIdUri(id: String): URI {
        return DefaultUriBuilderFactory()
                .uriString(arkivConfig.url)
                .path(PATH_HENT_JOURNALPOST_ID.format(id))
                .build()
    }


    companion object {
        private const val PATH_SEND_INN = "arkiv"
        private const val PATH_HENT_SAKSNUMMER = "/journalpost/%s/sak"
        private const val PATH_HENT_JOURNALPOST_ID = "/journalpost/kanalreferanseid/%s"
    }

}
