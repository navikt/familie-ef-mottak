package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.repository.SøknadRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity

internal class SøknadskvitteringControllerTest : IntegrasjonSpringRunnerTest() {
    @Autowired
    lateinit var søknadRepository: SøknadRepository

//    @Test
//    internal fun `Skal returnere 200 OK for å hente søknad med id`() {
//        val søknad =
//            søknadRepository.insert(
//                søknad(
//                    søknadJsonString = EncryptedString(objectMapper.writeValueAsString(Testdata.søknadOvergangsstønad)),
//                ),
//            )
//        val respons = hentSøknad(søknad.id)
//
//        assertThat(respons.statusCode).isEqualTo(HttpStatus.OK)
//        assertThat(respons.body).isNotNull()
//    }

    private fun hentSøknad(søknadId: String): ResponseEntity<ByteArray> =
        restTemplate.exchange(
            localhost("/api/soknadskvittering/$søknadId"),
            HttpMethod.GET,
            HttpEntity<ByteArray>(headers),
        )
}
