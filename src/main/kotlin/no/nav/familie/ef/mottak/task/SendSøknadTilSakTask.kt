package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, beskrivelse = "Send søknad til sak")
class SendSøknadTilSakTask(private val søknadService: SøknadService) : AsyncTaskStep {

    override fun doTask(task: Task) {
        søknadService.sendTilSak(task.payload)
    }

    companion object {

        const val SEND_SØKNAD_TIL_SAK = "sendSøknadTilSak"
    }
}