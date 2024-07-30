package no.nav.familie.ef.sak.felles.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import no.nav.familie.ef.mottak.personopplysninger.PdlClient
import no.nav.familie.ef.mottak.personopplysninger.PdlConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.web.client.RestOperations
import java.net.URI

class PdlClientTest {
    companion object {
        private val restOperations: RestOperations = RestTemplateBuilder().build()
        lateinit var pdlClient: PdlClient
        lateinit var wiremockServerItem: WireMockServer

        @BeforeAll
        @JvmStatic
        fun initClass() {
            wiremockServerItem = WireMockServer(wireMockConfig().dynamicPort())
            wiremockServerItem.start()
            pdlClient = PdlClient(PdlConfig(URI.create(wiremockServerItem.baseUrl())), restOperations)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            wiremockServerItem.stop()
        }
    }

    @AfterEach
    fun tearDownEachTest() {
        wiremockServerItem.resetAll()
    }

    @Test
    fun `pdlClient håndterer response for uthenting av aktørIDer`() {
        wiremockServerItem.stubFor(
            post(urlEqualTo("/${PdlConfig.PATH_GRAPHQL}"))
                .willReturn(okJson(readFile("hent_identer.json"))),
        )
        val response = pdlClient.hentAktørIder("12345")
        assertThat(response.identer.first().ident).isEqualTo("12345678901")
    }

    @Test
    fun `pdlClient håndterer response for uthenting av identer`() {
        wiremockServerItem.stubFor(
            post(urlEqualTo("/${PdlConfig.PATH_GRAPHQL}"))
                .willReturn(okJson(readFile("hent_identer.json"))),
        )
        val response = pdlClient.hentPersonidenter("12345")
        assertThat(response.identer.first().ident).isEqualTo("12345678901")
    }

    private fun readFile(filnavn: String): String = this::class.java.getResource("/json/$filnavn").readText()
}
