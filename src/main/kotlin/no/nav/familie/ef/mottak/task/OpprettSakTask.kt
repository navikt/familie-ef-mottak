package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.SakService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = OpprettSakTask.TYPE,
                     maxAntallFeil = 100,
                     beskrivelse = "Oppretter oppgave i GoSys/Infotrygd")
class OpprettSakTask(private val taskRepository: TaskRepository,
private val sakService: SakService) : AsyncTaskStep {


    override fun doTask(task: Task) {

        sakService.opprettSakOmIngenFinnes(task.payload)
    }

    override fun onCompletion(task: Task) {
        val nesteTask = Task.nyTask(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, task.payload, task.metadata)
        taskRepository.save(nesteTask)

    }

    companion object {

        const val TYPE = "opprettSak"
    }
}
