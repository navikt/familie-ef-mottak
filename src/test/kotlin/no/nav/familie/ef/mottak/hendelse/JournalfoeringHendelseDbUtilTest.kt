package no.nav.familie.ef.mottak.hendelse

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.mottak.repository.HendelsesloggRepository
import no.nav.familie.ef.mottak.repository.TaskRepositoryUtvidet
import no.nav.familie.ef.mottak.task.LagEksternJournalføringsoppgaveTask
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class JournalfoeringHendelseDbUtilTest {

    private val hendelsesloggRepository = mockk<HendelsesloggRepository>()
    private val taskRepositoryUtvidet = mockk<TaskRepositoryUtvidet>()
    private val taskService = mockk<TaskService>()
    private val journalfoeringHendelseDbUtil =
        JournalfoeringHendelseDbUtil(hendelsesloggRepository, taskService, taskRepositoryUtvidet)

    @Test
    internal fun `skal kunne prosessere journalføringshendelse selv om den mangler bruker`() {
        val slot = slot<Task>()
        every {
            taskService.save(capture(slot))
        } answers {
            slot.captured
        }

        journalfoeringHendelseDbUtil.lagreEksternJournalføringsTask(
            Journalpost(
                journalpostId = "123",
                journalposttype = Journalposttype.I,
                journalstatus = Journalstatus.MOTTATT,
                tema = "ENF",
                behandlingstema = "abcdef"
            )
        )
        verify(exactly = 1) { taskService.save(any()) }
        Assertions.assertThat(slot.captured.type).isEqualTo(LagEksternJournalføringsoppgaveTask.TYPE)
        Assertions.assertThat(slot.captured.metadata["personIdent"]).isEqualTo("Ukjent")
    }
}
