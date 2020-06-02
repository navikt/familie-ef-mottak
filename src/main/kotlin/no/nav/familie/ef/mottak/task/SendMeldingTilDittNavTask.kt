package no.nav.familie.ef.mottak.task

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = SendMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV,
                     beskrivelse = "Send melding til ditt nav")
class SendMeldingTilDittNavTask(private val taskRepository: TaskRepository) : AsyncTaskStep {


    override fun doTask(task: Task) {
        // TODO
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(Task.nyTask(SlettSøknadFraMottakTask.SLETT_SØKNAD_FRA_MOTTAK_TASK, task.payload, task.metadata))
    }

    companion object {
        const val SEND_MELDING_TIL_DITT_NAV = "sendMeldingTilDittNav"
    }

}