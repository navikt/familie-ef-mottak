package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.ef.mottak.service.Testdata
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

internal class DokumentasjonsbehovControllerTest : IntegrasjonSpringRunnerTest() {

    @Autowired lateinit var søknadService: SøknadService

    @Test
    internal fun `fnr i token er lik fnr i søknaden`() {
        val søknad = SøknadMedVedlegg(Testdata.søknadOvergangsstønad, listOf())
        val kvittering = søknadService.mottaOvergangsstønad(søknad)
        headers.setBearerAuth(getTestToken(søknad.søknad.personalia.verdi.fødselsnummer.verdi.verdi))

        val response: ResponseEntity<Any> =
            restTemplate.exchange(
                localhost("/api/soknad/dokumentasjonsbehov/${kvittering.id}"),
                HttpMethod.GET,
                HttpEntity<Any>(headers)
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    internal fun `fnr i token er ikke lik fnr i søknaden`() {

        val søknad = SøknadMedVedlegg(Testdata.søknadOvergangsstønad, listOf())
        val kvittering = søknadService.mottaOvergangsstønad(søknad)

        headers.setBearerAuth(lokalTestToken)

        val response: ResponseEntity<Any> =
            restTemplate.exchange(
                localhost("/api/soknad/dokumentasjonsbehov/${kvittering.id}"),
                HttpMethod.GET,
                HttpEntity<Any>(headers)
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }
}
