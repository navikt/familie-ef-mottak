package no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.EttersendingVedleggRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.EncryptedFile
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientResponseException
import java.time.LocalDateTime
import java.util.UUID

internal class ArkiveringServiceTest {

    private val integrasjonerClient: IntegrasjonerClient = mockk()
    private val søknadService: SøknadService = mockk()
    private val ettersendingService: EttersendingService = mockk(relaxed = true)
    private val vedleggRepository: VedleggRepository = mockk()
    private val ettersendingVedleggRepository: EttersendingVedleggRepository = mockk(relaxed = true)

    val arkiveringService = ArkiveringService(
        integrasjonerClient,
        søknadService,
        ettersendingService,
        vedleggRepository,
        ettersendingVedleggRepository,
    )

    private val ettersending = Ettersending(
        id = UUID.randomUUID(),
        ettersendingJson = EncryptedString(data = ""),
        stønadType = StønadType.OVERGANGSSTØNAD.name,
        fnr = "",
        taskOpprettet = false,
        opprettetTid = LocalDateTime.now(),
        ettersendingPdf = EncryptedFile("pdf".toByteArray()),
    )

    private val conflictException =
        HttpClientErrorException.Conflict.create(null, HttpStatus.CONFLICT, null, null, null, null)

    @Test
    internal fun `Skal håndtere 409 feil fra integrasjoner ved arkivering av vedlegg som allerede er journalført med callId`() {
        val forventetJounalføringsId = "123"
        val forventetFeil = lagRessursException(conflictException)
        val journalposter = listOf(lagJournalpost(forventetJounalføringsId, "callId"))
        every { ettersendingService.hentEttersending(any()) } returns ettersending
        every { integrasjonerClient.arkiver(any()) } throws forventetFeil
        every { integrasjonerClient.hentJournalposterForBruker(any()) } returns journalposter
        val journalførEttersending = arkiveringService.journalførEttersending("456", "callId")
        Assertions.assertThat(journalførEttersending).isEqualTo(forventetJounalføringsId)
    }

    @Test
    internal fun `Skal ikke prøve å håndtere feil som ikke er av type conflict-409 `() {
        val feil = HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR)
        every { ettersendingService.hentEttersending(any()) } returns ettersending
        every { integrasjonerClient.arkiver(any()) } throws lagRessursException(feil)
        val ressursException =
            assertThrows<RessursException> { arkiveringService.journalførEttersending("456", "callId") }
        Assertions.assertThat(ressursException.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        verify(exactly = 0) { integrasjonerClient.hentJournalposterForBruker(any()) }
    }

    private fun lagRessursException(restClientResponseException: RestClientResponseException): RessursException {
        return RessursException(
            cause = restClientResponseException,
            ressurs = Ressurs.failure("feil"),
            httpStatus = HttpStatus.valueOf(restClientResponseException.rawStatusCode),
        )
    }

    private fun lagJournalpost(forventetJournalpostId: String, eksternReferanseId: String) = Journalpost(
        journalpostId = forventetJournalpostId,
        journalposttype = Journalposttype.I,
        journalstatus = Journalstatus.MOTTATT,
        dokumenter = listOf(),
        relevanteDatoer = listOf(),
        eksternReferanseId = eksternReferanseId,
    )
}
