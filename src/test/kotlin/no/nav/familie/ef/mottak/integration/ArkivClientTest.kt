package no.nav.familie.ef.mottak.integration

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.mottak.config.ClientConfigurationPropertiesLocal
import no.nav.familie.ef.mottak.config.IntegrasjonerConfig
import no.nav.familie.ef.mottak.integration.dto.ArkiverDokumentResponse
import no.nav.familie.ef.mottak.integration.dto.ArkiverSøknadRequest
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.failure
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange
import org.springframework.web.util.DefaultUriBuilderFactory
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

internal class ArkivClientTest {

    private val operations: RestOperations = mockk()
    private val integrasjonerConfig: IntegrasjonerConfig = IntegrasjonerConfig("mock/")
    private val oAuth2AccessTokenService: OAuth2AccessTokenService = mockk()
    private val clientConfigurationPropertiesLocal: ClientConfigurationPropertiesLocal = mockk()

    val arkivClient: ArkivClient =
            ArkivClient(operations, integrasjonerConfig, oAuth2AccessTokenService, clientConfigurationPropertiesLocal)

    val arkiverSøknadRequest = ArkiverSøknadRequest("123456789", true, listOf())
    val arkiverDokumentResponse: ArkiverDokumentResponse = ArkiverDokumentResponse("wer", true)
    val uri = DefaultUriBuilderFactory().uriString(integrasjonerConfig.url).path(ArkivClient.PATH_SEND_INN).build()


    @Test
    fun `Skal arkivere  søknad`() {
        // Gitt
        operationMockSkalReturnere(success(arkiverDokumentResponse))
        // Vil gi reultat
        assertNotNull(arkivClient.arkiver(arkiverSøknadRequest))

    }

    @Test
    fun `Skal ikke arkivere  søknad`() {
        operationMockSkalReturnere(failure("error"))
        assertFailsWith(IllegalStateException::class) {
            arkivClient.arkiver(arkiverSøknadRequest)
        }
    }

    private fun operationMockSkalReturnere(ressurs: Ressurs<ArkiverDokumentResponse>) {
        every {
            operations.exchange<Ressurs<ArkiverDokumentResponse>>(url = uri,
                                                                  method = HttpMethod.POST,
                                                                  requestEntity = HttpEntity(arkiverSøknadRequest))
        } returns ResponseEntity.ok().body(ressurs)
    }
}



