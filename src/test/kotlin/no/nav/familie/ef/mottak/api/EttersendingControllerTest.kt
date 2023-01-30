package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.kontrakter.ef.ettersending.Dokumentasjonsbehov
import no.nav.familie.kontrakter.ef.ettersending.EttersendelseDto
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import no.nav.familie.kontrakter.felles.ef.StønadType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.UUID

internal class EttersendingControllerTest : IntegrasjonSpringRunnerTest() {

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    internal fun `kall for å sende inn ettersending går fint`() {
        val response: ResponseEntity<Any> = restTemplate.exchange(
            localhost("/api/ettersending"),
            HttpMethod.POST,
            HttpEntity(request(), headers),
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    private fun request(): Map<StønadType, EttersendelseDto> {
        val vedlegg1 = Vedlegg(UUID.randomUUID().toString(), "Vedlegg 1", "Vedleggtittel 1")
        val personIdent = "12345678901"
        return mapOf(
            StønadType.OVERGANGSSTØNAD to
                EttersendelseDto(
                    listOf(
                        Dokumentasjonsbehov(
                            id = UUID.randomUUID().toString(),
                            søknadsdata = null,
                            dokumenttype = "DOKUMENTASJON_LÆRLING",
                            beskrivelse = "Lærlingekontrakt",
                            stønadType = StønadType.OVERGANGSSTØNAD,
                            innsendingstidspunkt = null,
                            vedlegg = listOf(vedlegg1),
                        ),
                    ),
                    personIdent = personIdent,
                ),
        )
    }
}
