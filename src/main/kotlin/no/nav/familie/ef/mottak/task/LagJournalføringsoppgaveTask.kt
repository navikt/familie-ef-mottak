package no.nav.familie.ef.mottak.task

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(
    taskStepType = LagJournalføringsoppgaveTask.TYPE,
    beskrivelse = "Lager oppgave i GoSys",
)
class LagJournalføringsoppgaveTask(
    private val taskService: TaskService,
    private val oppgaveService: OppgaveService,
) : AsyncTaskStep {
    val antallTilManuellJournalføring: Counter = Metrics.counter("alene.med.barn.manueltjournalfort")

    override fun doTask(task: Task) {
        val oppgaveId = oppgaveService.lagJournalføringsoppgaveForSøknadId(task.payload)
        oppgaveId?.let {
            task.metadata.apply {
                this[JOURNALFØRING_OPPGAVE_ID_KEY] = oppgaveId.toString()
            }
        }
    }

    override fun onCompletion(task: Task) {
        antallTilManuellJournalføring.increment()
        task.metadata[JOURNALFØRING_OPPGAVE_ID_KEY]?.let {
            taskService.save(
                Task(
                    TaskType(TYPE).nesteManuellflytTask(),
                    task.payload,
                    task.metadata,
                ),
            )
        }
    }

    companion object {
        const val JOURNALFØRING_OPPGAVE_ID_KEY = "journalføringOppgaveId"
        const val TYPE = "lagJournalføringsoppgave"
    }
}
