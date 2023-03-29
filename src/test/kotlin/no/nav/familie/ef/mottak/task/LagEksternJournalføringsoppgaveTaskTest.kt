package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.journalpost.Bruker
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus.FERDIGSTILT
import no.nav.familie.prosessering.domene.Task
import org.junit.jupiter.api.Test
import java.util.Properties

internal class LagEksternJournalføringsoppgaveTaskTest {

    private val ettersendingRepository = mockk<EttersendingRepository>()
    private val søknadRepository = mockk<SøknadRepository>()
    private val oppgaveServie = mockk<OppgaveService>()
    private val journalpostClient = mockk<IntegrasjonerClient>()
    private val lagEksternJournalføringsoppgaveTask = LagEksternJournalføringsoppgaveTask(
        ettersendingRepository,
        oppgaveServie,
        søknadRepository,
        journalpostClient,
    )

    private val journalpost = Journalpost(
        "123",
        Journalposttype.I,
        Journalstatus.MOTTATT,
        tema = "ENF",
        behandlingstema = "abc123",
        bruker = Bruker("213", BrukerIdType.FNR),
        kanal = "NAV_NO",
    )

    @Test
    internal fun `skal opprette ekstern journalføringstask`() {
        every { ettersendingRepository.findByJournalpostId(any()) } returns null
        every { søknadRepository.findByJournalpostId(any()) } returns null
        every { oppgaveServie.lagJournalføringsoppgaveForJournalpostId(any()) } returns 1
        every { journalpostClient.hentJournalpost(any()) } returns journalpost

        lagEksternJournalføringsoppgaveTask.doTask(
            Task(
                type = LagEksternJournalføringsoppgaveTask.TYPE,
                payload = "123",
                properties = Properties(),
            ),
        )

        verify { oppgaveServie.lagJournalføringsoppgaveForJournalpostId("123") }
    }

    @Test
    internal fun `skal ikke opprette ekstern journalføringstask hvis det finnes en søknad for journalposten`() {
        every { ettersendingRepository.findByJournalpostId(any()) } returns null
        every { søknadRepository.findByJournalpostId(any()) } returns søknad()
        every { oppgaveServie.lagJournalføringsoppgaveForJournalpostId(any()) } returns 1
        every { journalpostClient.hentJournalpost(any()) } returns journalpost

        lagEksternJournalføringsoppgaveTask.doTask(
            Task(
                type = LagEksternJournalføringsoppgaveTask.TYPE,
                payload = "123",
                properties = Properties(),
            ),
        )

        verify(inverse = true) { oppgaveServie.lagJournalføringsoppgaveForJournalpostId(any()) }
    }

    @Test
    internal fun `skal ikke opprette ekstern journalføringstask hvis det finnes en ettersending for journalposten`() {
        every { ettersendingRepository.findByJournalpostId(any()) } returns
            Ettersending(
                ettersendingJson = EncryptedString("{a:1}"),
                stønadType = "OVERGANGSSTØNAD",
                journalpostId = "1",
                fnr = "",
                taskOpprettet = false,
            )
        every { søknadRepository.findByJournalpostId(any()) } returns null
        every { oppgaveServie.lagJournalføringsoppgaveForJournalpostId(any()) } returns 1
        every { journalpostClient.hentJournalpost(any()) } returns journalpost

        lagEksternJournalføringsoppgaveTask.doTask(
            Task(
                type = LagEksternJournalføringsoppgaveTask.TYPE,
                payload = "123",
                properties = Properties(),
            ),
        )

        verify(inverse = true) { oppgaveServie.lagJournalføringsoppgaveForJournalpostId(any()) }
    }

    @Test
    internal fun `skal ikke opprette ekstern journalføringstask hvis journalposten er ferdigstilt`() {
        every { ettersendingRepository.findByJournalpostId(any()) } returns
            Ettersending(
                ettersendingJson = EncryptedString("{a:1}"),
                stønadType = "OVERGANGSSTØNAD",
                journalpostId = "1",
                fnr = "",
                taskOpprettet = false,
            )
        every { søknadRepository.findByJournalpostId(any()) } returns null
        every { oppgaveServie.lagJournalføringsoppgaveForJournalpostId(any()) } returns 1
        every { journalpostClient.hentJournalpost(any()) } returns journalpost.copy(journalstatus = FERDIGSTILT)

        lagEksternJournalføringsoppgaveTask.doTask(
            Task(
                type = LagEksternJournalføringsoppgaveTask.TYPE,
                payload = "123",
                properties = Properties(),
            ),
        )

        verify(inverse = true) { oppgaveServie.lagJournalføringsoppgaveForJournalpostId(any()) }
    }
}
