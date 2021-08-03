package no.nav.familie.ef.mottak.task

import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.Test
import java.util.Properties
import java.util.UUID

internal class LagJournalføringsoppgaveTaskTest {

    private val taskRepository: TaskRepository = mockk()
    private val oppgaveService: OppgaveService = mockk(relaxed = true)
    private val lagJournalføringsoppgaveTask: LagJournalføringsoppgaveTask =
            LagJournalføringsoppgaveTask(taskRepository, oppgaveService)


    @Test
    fun `skal kalle lagJournalføringsoppgaveForSøknadId hvis task payload er gyldig uuid`() {
        val uuid = UUID.randomUUID().toString()

        lagJournalføringsoppgaveTask.doTask(Task(type = "", payload = uuid, properties = Properties()))

        verify { oppgaveService.lagJournalføringsoppgaveForSøknadId(uuid) }
    }


}
