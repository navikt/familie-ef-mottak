package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = SlettSøknadFraMottakTask.SLETT_SØKNAD_FRA_MOTTAK_TASK,
                     beskrivelse = "Slett ferdigbehandlet søknad fra database")
class SlettSøknadFraMottakTask(private val soknadRepository: SoknadRepository) : AsyncTaskStep {


    override fun doTask(task: Task) {
        soknadRepository.deleteById(task.payload)
    }

    override fun onCompletion(task: Task) { // Dette er siste Task i mottaksflyten.
    }

    companion object {
        const val SLETT_SØKNAD_FRA_MOTTAK_TASK = "slettSøknadFraMottak"
    }

}