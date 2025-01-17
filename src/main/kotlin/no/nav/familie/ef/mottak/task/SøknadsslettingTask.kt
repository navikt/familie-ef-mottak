package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.SøknadskvitteringService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@TaskStepBeskrivelse(
    taskStepType = SøknadsslettingTask.TYPE,
    beskrivelse = "Sletter en søknad fra mottak",
)
class SøknadsslettingTask(
    private val søknadskvitteringService: SøknadskvitteringService,
) : AsyncTaskStep {
    @Transactional
    override fun doTask(task: Task) {
        søknadskvitteringService.slettSøknad(task.payload)
    }

    companion object {
        const val TYPE = "søknadssletting"
    }
}
