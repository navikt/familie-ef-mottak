package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.service.Testdata.søknadBarnetilsyn
import no.nav.familie.ef.mottak.service.Testdata.søknadOvergangsstønad
import no.nav.familie.ef.mottak.service.Testdata.søknadSkolepenger
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.UUID

internal class SøknadskvitteringControllerTest : IntegrasjonSpringRunnerTest() {
    @Autowired
    lateinit var søknadRepository: SøknadRepository

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(søkerBearerToken())
    }

    @Test
    internal fun `overgangsstønad ok request`() {
        verifySøknadMedVedleggRequest(
            SøknadMedVedlegg(søknadOvergangsstønad, listOf(lagVedlegg(), lagVedlegg())),
            "/api/soknad/overgangsstonad",
        )
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
    internal fun `vedlegg savnes i listen med vedlegg`() {
        val request = SøknadMedVedlegg(søknadOvergangsstønad, listOf(lagVedlegg("finnes_ikke")))
        val response: ResponseEntity<Any> =
            restTemplate.exchange(
                localhost("/api/soknad/overgangsstonad"),
                HttpMethod.POST,
                HttpEntity(request, headers),
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun <T> verifySøknadMedVedleggRequest(
        søknad: SøknadMedVedlegg<T>,
        url: String,
        forventetHttpStatus: HttpStatus = HttpStatus.OK,
    ) {
        val response: ResponseEntity<Any> =
            restTemplate.exchange(
                localhost(url),
                HttpMethod.POST,
                HttpEntity(søknad, headers),
            )

        assertThat(response.statusCode).isEqualTo(forventetHttpStatus)
    }

    private fun lagVedlegg(id: String = UUID.randomUUID().toString()) = Vedlegg(id, "navn", "tittel")
}
