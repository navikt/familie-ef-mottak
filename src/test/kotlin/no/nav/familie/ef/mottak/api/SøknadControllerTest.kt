package no.nav.familie.ef.mottak.api

import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.ef.mottak.service.Testdata.søknadBarnetilsyn
import no.nav.familie.ef.mottak.service.Testdata.søknadOvergangsstønad
import no.nav.familie.ef.mottak.service.Testdata.søknadSkolepenger
import no.nav.familie.http.client.MultipartBuilder
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.exchange
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.http.*
import org.springframework.test.context.ActiveProfiles

@Profile("søknad-controller-test")
@Configuration
@Primary
class SøknadControllerTestConfig {

    @Bean fun søknadService(): SøknadService = mockk(relaxed = true)
}

@ActiveProfiles("local", "søknad-controller-test")
internal class SøknadControllerTest : IntegrasjonSpringRunnerTest() {

    @Autowired lateinit var søknadService: SøknadService

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    internal fun `overgangsstønad ok request`() {
        val søknad = SøknadMedVedlegg(søknadOvergangsstønad, listOf(lagVedlegg("1"), lagVedlegg("2")))
        okSøknadMedVedleggRequest(søknad, "/api/soknad")
        okSøknadMedVedleggRequest(søknad, "/api/soknad/overgangsstonad")
        verify(exactly = 2) { søknadService.mottaOvergangsstønad(søknad, any()) }
    }

    @Test
    internal fun `barnetilsyn ok request`() {
        val søknad = SøknadMedVedlegg(søknadBarnetilsyn, listOf(lagVedlegg("1"), lagVedlegg("2")))
        okSøknadMedVedleggRequest(søknad, "/api/soknad/barnetilsyn")
        verify(exactly = 1) { søknadService.mottaBarnetilsyn(søknad, any()) }
    }

    @Test
    internal fun `skolepenger ok request`() {
        val søknad = SøknadMedVedlegg(søknadSkolepenger, listOf(lagVedlegg("1"), lagVedlegg("2")))
        okSøknadMedVedleggRequest(søknad, "/api/soknad/skolepenger")
        verify(exactly = 1) { søknadService.mottaSkolepenger(søknad, any()) }
    }

    private fun <T> okSøknadMedVedleggRequest(søknad: SøknadMedVedlegg<T>,
                                              url: String) {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
        val request = MultipartBuilder()
                .withJson("søknad", søknad)
                .withByteArray("vedlegg", "1", byteArrayOf(12))
                .withByteArray("vedlegg", "2", byteArrayOf(12))
                .build()

        val response: ResponseEntity<Any> =
                restTemplate.exchange(localhost(url),
                                      HttpMethod.POST,
                                      HttpEntity(request, headers))

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    internal fun `vedlegg savnes i json`() {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
        val request = MultipartBuilder()
                .withJson("søknad", SøknadMedVedlegg(søknadOvergangsstønad, listOf(lagVedlegg("1"))))
                .build()
        val response: ResponseEntity<Any> =
                restTemplate.exchange(localhost("/api/soknad"),
                                      HttpMethod.POST,
                                      HttpEntity(request, headers))
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    internal fun `vedlegg savnes i listen med vedlegg`() {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
        val request = MultipartBuilder()
                .withJson("søknad", SøknadMedVedlegg(søknadOvergangsstønad, listOf(lagVedlegg("1"))))
                .withByteArray("vedlegg", "1", byteArrayOf(12))
                .withByteArray("vedlegg", "2", byteArrayOf(12))
                .build()

        val response: ResponseEntity<Any> =
                restTemplate.exchange(localhost("/api/soknad"),
                                      HttpMethod.POST,
                                      HttpEntity(request, headers))
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    private fun lagVedlegg(id: String) = Vedlegg(id, "navn", "tittel")
}