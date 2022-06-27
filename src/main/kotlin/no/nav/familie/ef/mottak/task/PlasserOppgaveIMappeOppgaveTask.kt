package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(
    taskStepType = PlasserOppgaveIMappeOppgaveTask.TYPE,
    beskrivelse = "Oppdater oppgave med riktig mappeId"
)
class PlasserOppgaveIMappeOppgaveTask(private val oppgaveService: OppgaveService) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val oppgaveId = task.metadata[LagJournalføringsoppgaveTask.journalføringOppgaveIdKey]?.toString()?.toLong()
            ?: error("Kan ikke finne oppgaveId")

        val søknadId = task.payload
        oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, søknadId)
    }

    companion object {

        const val TYPE = "plasserOppgaveIMappe"
    }
}
