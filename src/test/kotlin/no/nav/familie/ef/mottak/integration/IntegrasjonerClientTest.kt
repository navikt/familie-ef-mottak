package no.nav.familie.ef.mottak.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.mottak.config.IntegrasjonerConfig
import no.nav.familie.http.sts.StsRestClient
import no.nav.familie.kontrakter.felles.Ressurs.Companion.failure
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.web.client.RestOperations
import java.net.URI
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

internal class IntegrasjonerClientTest {

    private val wireMockServer = WireMockServer(wireMockConfig().dynamicPort())
    private val restOperations: RestOperations = RestTemplateBuilder().build()

    private lateinit var integrasjonerClient: IntegrasjonerClient
    private val arkiverSøknadRequest = ArkiverDokumentRequest("123456789", true, listOf())
    private val arkiverDokumentResponse: ArkiverDokumentResponse = ArkiverDokumentResponse("wer", true)

    @BeforeEach
    fun setUp() {
        wireMockServer.start()
        val stsRestClient = mockk<StsRestClient>()
        every { stsRestClient.systemOIDCToken } returns "token"
        integrasjonerClient = IntegrasjonerClient(restOperations, IntegrasjonerConfig(URI.create(wireMockServer.baseUrl())))
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.resetAll()
        wireMockServer.stop()
    }

    @Test
    fun `hentSaksnummer parser payload og returnerer saksnummer`() {
        wireMockServer.stubFor(get(urlEqualTo("/${IntegrasjonerClient.PATH_HENT_SAKSNUMMER}?journalpostId=123"))
                                       .willReturn(okJson(readFile("saksnummer.json"))))

        assertThat(integrasjonerClient.hentSaksnummer("123")).isEqualTo("140258871")
    }

    @Test
    fun `Skal arkivere  søknad`() {
        // Gitt
        wireMockServer.stubFor(post(urlEqualTo("/${IntegrasjonerClient.PATH_SEND_INN}"))
                                       .willReturn(okJson(success(arkiverDokumentResponse).toJson())))
        // Vil gi resultat
        assertNotNull(integrasjonerClient.arkiver(arkiverSøknadRequest))
    }

    @Test
    fun `Skal ikke arkivere søknad`() {
        wireMockServer.stubFor(post(urlEqualTo("/${IntegrasjonerClient.PATH_SEND_INN}"))
                                       .willReturn(okJson(failure<Any>("error").toJson())))

        assertFailsWith(IllegalStateException::class) {
            integrasjonerClient.arkiver(arkiverSøknadRequest)
        }
    }

    private fun readFile(filnavn: String): String {
        return this::class.java.getResource("/json/$filnavn").readText()
    }
}



