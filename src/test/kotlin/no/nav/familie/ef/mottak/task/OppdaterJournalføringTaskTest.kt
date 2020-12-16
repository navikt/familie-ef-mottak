package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.mottak.hendelse.JournalføringHendelseServiceTest
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.service.ArkiveringService
import no.nav.familie.ef.mottak.service.INFOTRYGD
import no.nav.familie.ef.mottak.service.SAKSTYPE_SØKNAD
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.ef.mottak.task.FerdigstillJournalføringTask
import no.nav.familie.ef.mottak.task.OppdaterJournalføringTask
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.dokarkiv.*
import no.nav.familie.kontrakter.felles.dokarkiv.Sak
import no.nav.familie.kontrakter.felles.journalpost.*
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import no.nav.familie.util.FnrGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.collections.HashMap

internal class OppdaterJournalføringTaskTest {

    private val integrasjonerClient: IntegrasjonerClient = mockk()
    private val søknadService: SøknadService = mockk()
    private val arkiveringService: ArkiveringService = ArkiveringService(integrasjonerClient, søknadService, mockk())
    private val taskRepository: TaskRepository = mockk()
    private val oppdaterJournalføringTask = OppdaterJournalføringTask(taskRepository, arkiveringService)

    @Test
    internal fun `skal ferdigstille journalføring med riktig id og enhet`() {
        val saksnummer = "A1B2C3"
        val journalpostIdSlot = slot<String>()
        val oppdaterJournalpostRequestSlot = slot<OppdaterJournalpostRequest>()

        every {
            søknadService.get("123")
        } returns Soknad(søknadJson = "",
                         dokumenttype = "noe",
                         saksnummer = saksnummer,
                         journalpostId = JournalføringHendelseServiceTest.JOURNALPOST_DIGITALSØKNAD,
                         fnr = FnrGenerator.generer())

        every {
            integrasjonerClient.oppdaterJournalpost(capture(oppdaterJournalpostRequestSlot), capture(journalpostIdSlot))
        } returns OppdaterJournalpostResponse("123")

        every {
            integrasjonerClient.hentJournalpost(any())
        } returns Journalpost(journalpostId = JournalføringHendelseServiceTest.JOURNALPOST_DIGITALSØKNAD,
                              journalposttype = Journalposttype.I,
                              journalstatus = Journalstatus.MOTTATT,
                              bruker = Bruker("123456789012", BrukerIdType.AKTOERID),
                              tema = "ENF",
                              kanal = "NAV_NO",
                              behandlingstema = null,
                              dokumenter = null,
                              journalforendeEnhet = null,
                              sak = null)


        oppdaterJournalføringTask.doTask(Task.nyTask(type = "", payload = "123", properties = Properties()))

        assertThat(journalpostIdSlot.captured).isEqualTo(JournalføringHendelseServiceTest.JOURNALPOST_DIGITALSØKNAD)
        assertThat(oppdaterJournalpostRequestSlot.captured.sak).isEqualTo(Sak(fagsakId = saksnummer,
                                                                              fagsaksystem = INFOTRYGD,
                                                                              sakstype = "FAGSAK"))
        assertThat(oppdaterJournalpostRequestSlot.captured.bruker!!.id).isEqualTo("123456789012")
        assertThat(oppdaterJournalpostRequestSlot.captured.bruker!!.idType).isEqualTo(IdType.AKTOERID)
    }

    @Test
    fun `Skal gå til FerdigstillOppgaveTask når FerdigstillJournalføringTask er utført`() {
        val slot = slot<Task>()
        every {
            taskRepository.save(capture(slot))
        } answers {
            slot.captured
        }

        oppdaterJournalføringTask.onCompletion(Task.nyTask(type = "", payload = "", properties = Properties()))

        Assertions.assertEquals(FerdigstillJournalføringTask.TYPE, slot.captured.taskStepType)
    }

}