package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.kontrakter.ef.ettersending.EttersendingDto
import no.nav.familie.kontrakter.ef.ettersending.EttersendingMedVedlegg
import no.nav.familie.kontrakter.ef.ettersending.EttersendingUtenSøknad
import no.nav.familie.kontrakter.ef.ettersending.Innsending
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.søknad.Dokument
import no.nav.familie.kontrakter.ef.søknad.Innsendingsdetaljer
import no.nav.familie.kontrakter.ef.søknad.Søknadsfelt
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.UUID

@ActiveProfiles("local")
internal class EttersendingControllerTest : IntegrasjonSpringRunnerTest() {

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    internal fun `kall for å sende inn ettersending går fint`() {
        val response: ResponseEntity<Any> = restTemplate.exchange(localhost("/api/ettersending"),
                                                                  HttpMethod.POST,
                                                                  HttpEntity(request(), headers))
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    private fun request(): EttersendingMedVedlegg {
        val innsendingsdetaljer = Søknadsfelt("detaljer", Innsendingsdetaljer(Søknadsfelt("dato", LocalDateTime.now())))
        val vedlegg1 = Vedlegg(UUID.randomUUID().toString(), "Vedlegg 1", "Vedleggtittel 1")
        val innsending1 = Innsending(beskrivelse = "Lærlingekontrakt",
                                     dokumenttype = "DOKUMENTASJON_LÆRLING",
                                     vedlegg = listOf(Dokument(vedlegg1.id, vedlegg1.navn)))
        val ettersendingUtenSøknad = EttersendingUtenSøknad(innsending = listOf(innsending1)
        )
        val ettersendingDto = EttersendingDto("12345678901", StønadType.OVERGANGSSTØNAD, null, ettersendingUtenSøknad)
        return EttersendingMedVedlegg(
                innsendingsdetaljer = innsendingsdetaljer,
                vedlegg = listOf(vedlegg1),
                ettersending = ettersendingDto
        )
    }
}