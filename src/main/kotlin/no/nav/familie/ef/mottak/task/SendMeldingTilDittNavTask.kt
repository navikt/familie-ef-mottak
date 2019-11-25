package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = SendMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV,
                     beskrivelse = "Send melding til ditt nav")
class SendMeldingTilDittNavTask(private val søknadService: SøknadService) : AsyncTaskStep {


    override fun doTask(task: Task) { //søknadService.sendTilSak(task.getPayload().getBytes());
    }

    override fun onCompletion(task: Task) { // Dette er siste Task i mottaks flyten.
    }

    companion object {
        const val SEND_MELDING_TIL_DITT_NAV = "sendMeldingTilDittNav"
    }

}