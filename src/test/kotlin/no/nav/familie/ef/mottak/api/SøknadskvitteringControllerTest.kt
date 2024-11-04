package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.service.Testdata
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

internal class SøknadskvitteringControllerTest : IntegrasjonSpringRunnerTest() {
    @Autowired
        lateinit var søknadRepository: SøknadRepository

    @Test
    internal fun `Skal returnere 200 OK for å hente søknad med id`() {
        val søknad =
            søknadRepository.insert(
                søknad(
                    søknadJsonString = EncryptedString(objectMapper.writeValueAsString(Testdata.søknadOvergangsstønad)),
                ),
            )
        val respons = hentSøknad(søknad.id)

        assertThat(respons.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(respons.body).isNotNull()
    }

    private fun hentSøknad(id: String): ResponseEntity<Map<String, Any>> =
        restTemplate.exchange(
            localhost("/api/soknadskvittering/$id"),
            HttpMethod.GET,
            HttpEntity<Map<String, Any>>(headers),
        )
}
