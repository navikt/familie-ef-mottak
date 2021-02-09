package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = LagEksternJournalføringsoppgaveTask.TYPE,
                     beskrivelse = "Lager oppgave i GoSys")
class LagEksternJournalføringsoppgaveTask(private val taskRepository: TaskRepository,
                                          private val oppgaveService: OppgaveService
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        oppgaveService.lagJournalføringsoppgaveForJournalpostId(task.payload)
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(Task(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, task.payload, task.metadata))
        // TODO sende melding til ditt nav om mottatt søknad? SendSøknadMottattTilDittNavTask
    }


    companion object {

        const val TYPE = "lagEksternJournalføringsoppgave"
    }

}
