package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, beskrivelse = "Send søknad til sak")
class SendSøknadTilSakTask(private val søknadService: SøknadService,
                           private val taskRepository: TaskRepository) : AsyncTaskStep {

    override fun doTask(task: Task) {
        søknadService.sendTilSak(task.payload)
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(Task.nyTask(SendMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV, task.payload, task.metadata))
    }

    companion object {

        const val SEND_SØKNAD_TIL_SAK = "sendSøknadTilSak"
    }
}