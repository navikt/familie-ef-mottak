package no.nav.familie.ef.mottak.integration

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.mottak.config.IntegrasjonerConfig
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.failure
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.arkivering.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.arkivering.ArkiverDokumentResponse
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange
import org.springframework.web.util.DefaultUriBuilderFactory
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

internal class IntegrasjonerClientTest {

    private val operations: RestOperations = mockk()
    private val integrasjonerConfig: IntegrasjonerConfig = IntegrasjonerConfig("mock/")

    private val integrasjonerClient: IntegrasjonerClient = IntegrasjonerClient(operations, integrasjonerConfig)

    private val arkiverSøknadRequest = ArkiverDokumentRequest("123456789", true, listOf())
    private val arkiverDokumentResponse: ArkiverDokumentResponse = ArkiverDokumentResponse("wer", true)
    private val uri = DefaultUriBuilderFactory().uriString(integrasjonerConfig.url).path(IntegrasjonerClient.PATH_SEND_INN).build()


    @Test
    fun `Skal arkivere  søknad`() {
        // Gitt
        operationMockSkalReturnere(success(arkiverDokumentResponse))
        // Vil gi resultat
        assertNotNull(integrasjonerClient.arkiver(arkiverSøknadRequest))

    }

    @Test
    fun `Skal ikke arkivere  søknad`() {
        operationMockSkalReturnere(failure("error"))
        assertFailsWith(IllegalStateException::class) {
            integrasjonerClient.arkiver(arkiverSøknadRequest)
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



