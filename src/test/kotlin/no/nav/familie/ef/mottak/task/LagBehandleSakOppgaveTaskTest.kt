package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.ef.mottak.hendelse.JournalføringHendelseServiceTest
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.ef.mottak.service.SakService
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.kontrakter.felles.journalpost.*
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import no.nav.familie.util.FnrGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

internal class LagBehandleSakOppgaveTaskTest {

    private val integrasjonerClient: IntegrasjonerClient = mockk()
    private val søknadService: SøknadService = mockk()
    private val oppgaveService: OppgaveService = mockk()
    private val sakService: SakService = mockk()
    private val taskRepository: TaskRepository = mockk()
    private val lagBehandleSakOppgaveTask =
            LagBehandleSakOppgaveTask(oppgaveService, søknadService, integrasjonerClient, sakService, taskRepository)

    @Test
    internal fun `skal lage behandle-sak-oppgave dersom det ikke finnes infotrygdsak fra før`() {
        val taskSlot = slot<Task>()

        every { sakService.kanOppretteInfotrygdSak(any()) } returns true
        every { søknadService.get("123L") } returns mockSøknad()
        every { integrasjonerClient.hentJournalpost(any()) } returns mockJournalpost()
        every { oppgaveService.lagBehandleSakOppgave(any(), "familie-ef-sak-blankett") } returns 99L
        every { taskRepository.save(capture(taskSlot)) }.answers { taskSlot.captured }

        lagBehandleSakOppgaveTask.doTask(Task(type = "", payload = "123L", properties = Properties()))
        verify(exactly = 1) {
            oppgaveService.lagBehandleSakOppgave(any(), "familie-ef-sak-blankett")
        }
        assertThat(taskSlot.captured.metadata[LagBehandleSakOppgaveTask.behandleSakOppgaveIdKey]).isEqualTo("99")
    }

    @Test
    internal fun `skal ikke lage behandle-sak-oppgave dersom infotrygdsak finnes fra før`() {
        every { sakService.kanOppretteInfotrygdSak(any()) } returns false
        every { søknadService.get("123L") } returns mockSøknad()
        every { integrasjonerClient.hentJournalpost(any()) } returns mockJournalpost()
        
        lagBehandleSakOppgaveTask.doTask(Task(type = "", payload = "123L", properties = Properties()))
        verify(exactly = 0) {
            oppgaveService.lagBehandleSakOppgave(any(), "")
        }
    }

    private fun mockJournalpost(): Journalpost {
        return Journalpost(journalpostId = JournalføringHendelseServiceTest.JOURNALPOST_DIGITALSØKNAD,
                           journalposttype = Journalposttype.I,
                           journalstatus = Journalstatus.MOTTATT,
                           bruker = Bruker("123456789012", BrukerIdType.AKTOERID),
                           tema = "ENF",
                           kanal = "NAV_NO",
                           behandlingstema = null,
                           dokumenter = null,
                           journalforendeEnhet = null,
                           sak = null)
    }

    private fun mockSøknad(): Soknad {
        return Soknad(søknadJson = "",
                      dokumenttype = DOKUMENTTYPE_SKOLEPENGER,
                      saksnummer = null,
                      journalpostId = JournalføringHendelseServiceTest.JOURNALPOST_DIGITALSØKNAD,
                      fnr = FnrGenerator.generer())
    }

}