package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.service.ArkiveringService
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import no.nav.familie.util.FnrGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.collections.HashMap

internal class FerdigstillJournalføringTaskTest {

    private val integrasjonerClient: IntegrasjonerClient = mockk()
    private val søknadService: SøknadService = mockk()
    private val arkiveringService: ArkiveringService = ArkiveringService(integrasjonerClient, søknadService, mockk())
    private val taskRepository: TaskRepository = mockk()
    private val ferdigstillJournalføringTask = FerdigstillJournalføringTask(arkiveringService, taskRepository)

    @Test
    internal fun `skal ferdigstille journalføring med riktig id og enhet`() {
        val journalpostId = "123"
        val enhetId = "1234"
        val journalpostIdSlot = slot<String>()
        val enhetSlot = slot<String>()

        every {
            søknadService.get("123L")
        } returns Soknad(søknadJson = "",
                         dokumenttype = "noe",
                         journalpostId = journalpostId,
                         fnr = FnrGenerator.generer())

        every {
            integrasjonerClient.finnBehandlendeEnhet(any())
        } returns listOf(Enhet(enhetId, ""))

        every {
            integrasjonerClient.ferdigstillJournalpost(capture(journalpostIdSlot), capture(enhetSlot))
        } returns HashMap()

        ferdigstillJournalføringTask.doTask(Task.nyTask(type = "", payload = "123L", properties = Properties()))

        assertThat(journalpostIdSlot.captured).isEqualTo(journalpostId)
        assertThat(enhetSlot.captured).isEqualTo(enhetId)
    }

    @Test
    fun `Skal gå til FerdigstillOppgaveTask når FerdigstillJournalføringTask er utført`() {
        val slot = slot<Task>()
        every {
            taskRepository.save(capture(slot))
        } answers {
            slot.captured
        }

        ferdigstillJournalføringTask.onCompletion(Task.nyTask(type = "", payload = "", properties = Properties()))

        Assertions.assertEquals(FerdigstillOppgaveTask.TYPE, slot.captured.taskStepType)
    }

}