package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.service.Testdata.søknad
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.*
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap


@ActiveProfiles("local")
internal class SøknadControllerTest : IntegrasjonSpringRunnerTest() {

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    internal fun `ok request`() {
        val multipartRequest: MultiValueMap<String, Any> = LinkedMultiValueMap()
        headers.set("Content-type", MediaType.MULTIPART_FORM_DATA_VALUE)

        val jsonHeaders = HttpHeaders()
        jsonHeaders.set("Content-type", MediaType.APPLICATION_JSON_VALUE)
        multipartRequest.add("søknad",
                             HttpEntity(SøknadMedVedlegg(søknad, listOf(lagVedlegg(1), lagVedlegg(2))), jsonHeaders))

        multipartRequest.add("vedlegg", lagVedleggRequest(1))
        multipartRequest.add("vedlegg", lagVedleggRequest(2))

        val response: ResponseEntity<Any> =
                restTemplate.exchange(localhost("/api/soknad"),
                                      HttpMethod.POST,
                                      HttpEntity(multipartRequest, headers))
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    internal fun `vedlegg savnes i json`() {
        val multipartRequest: MultiValueMap<String, Any> = LinkedMultiValueMap()
        headers.set("Content-type", MediaType.MULTIPART_FORM_DATA_VALUE)

        val jsonHeaders = HttpHeaders()
        jsonHeaders.set("Content-type", MediaType.APPLICATION_JSON_VALUE)
        multipartRequest.add("søknad",
                             HttpEntity(SøknadMedVedlegg(søknad, listOf(lagVedlegg(1))),
                                        jsonHeaders))
        val response: ResponseEntity<Any> =
                restTemplate.exchange(localhost("/api/soknad"),
                                      HttpMethod.POST,
                                      HttpEntity(multipartRequest, headers))
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    internal fun `vedlegg savnes i listen med vedlegg`() {
        val multipartRequest: MultiValueMap<String, Any> = LinkedMultiValueMap()
        headers.set("Content-type", MediaType.MULTIPART_FORM_DATA_VALUE)

        val jsonHeaders = HttpHeaders()
        jsonHeaders.set("Content-type", MediaType.APPLICATION_JSON_VALUE)
        multipartRequest.add("søknad",
                             HttpEntity(SøknadMedVedlegg(søknad, listOf()),
                                        jsonHeaders))
        multipartRequest.add("vedlegg", lagVedleggRequest(1))

        val response: ResponseEntity<Any> =
                restTemplate.exchange(localhost("/api/soknad"),
                                      HttpMethod.POST,
                                      HttpEntity(multipartRequest, headers))
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    private fun lagVedleggRequest(id: Int): ByteArrayResource {
        return object : ByteArrayResource("vedlegg$id".toByteArray()) {
            override fun getFilename(): String? {
                return id.toString()
            }
        }
    }

    private fun lagVedlegg(id: Int) = Vedlegg(id.toString(), "navn", "tittel", byteArrayOf())
}