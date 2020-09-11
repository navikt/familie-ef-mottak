package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.service.Testdata.søknadBarnetilsyn
import no.nav.familie.ef.mottak.service.Testdata.søknadOvergangsstønad
import no.nav.familie.ef.mottak.service.Testdata.søknadSkolepenger
import no.nav.familie.http.client.MultipartBuilder
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.*
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ActiveProfiles("local")
internal class SøknadControllerTest : IntegrasjonSpringRunnerTest() {

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    internal fun `overgangsstønad ok request`() {
        verifySøknadMedVedleggRequest(SøknadMedVedlegg(søknadOvergangsstønad, listOf(lagVedlegg(), lagVedlegg())), "/api/soknad")
        verifySøknadMedVedleggRequest(SøknadMedVedlegg(søknadOvergangsstønad, listOf(lagVedlegg(), lagVedlegg())),
                                      "/api/soknad/overgangsstonad")
    }

    @Test
    internal fun `barnetilsyn ok request`() {
        val søknad = SøknadMedVedlegg(søknadBarnetilsyn, listOf(lagVedlegg(), lagVedlegg()))
        verifySøknadMedVedleggRequest(søknad, "/api/soknad/barnetilsyn")
    }

    @Test
    internal fun `skolepenger ok request`() {
        val søknad = SøknadMedVedlegg(søknadSkolepenger, listOf(lagVedlegg(), lagVedlegg()))
        verifySøknadMedVedleggRequest(søknad, "/api/soknad/skolepenger")
    }

    @Test
    internal fun `det skal ikke være mulig å sende inn samme vedlegg på nytt med ny søknad`() {
        val vedleggId = UUID.randomUUID().toString()
        val søknad = SøknadMedVedlegg(søknadOvergangsstønad, listOf(lagVedlegg(vedleggId)))
        verifySøknadMedVedleggRequest(søknad, "/api/soknad/overgangsstonad")
        verifySøknadMedVedleggRequest(søknad, "/api/soknad/overgangsstonad", HttpStatus.BAD_REQUEST)
    }

    @Test
    internal fun `vedlegg savnes i json`() {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
        val request = MultipartBuilder()
                .withJson("søknad", SøknadMedVedlegg(søknadOvergangsstønad, listOf(lagVedlegg())))
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

    private fun <T> verifySøknadMedVedleggRequest(søknad: SøknadMedVedlegg<T>,
                                                  url: String,
                                                  forventetHttpStatus: HttpStatus = HttpStatus.OK) {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
        val multipartBuilder = MultipartBuilder()
                .withJson("søknad", søknad)

        søknad.vedlegg.forEach {
            multipartBuilder.withByteArray("vedlegg", it.id, byteArrayOf(12))
        }

        val response: ResponseEntity<Any> =
                restTemplate.exchange(localhost(url),
                                      HttpMethod.POST,
                                      HttpEntity(multipartBuilder.build(), headers))

        assertThat(response.statusCode).isEqualTo(forventetHttpStatus)
    }

    private fun lagVedlegg(id: String = UUID.randomUUID().toString()) = Vedlegg(id, "navn", "tittel")
}