package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDateTime

@Service
@TaskStepBeskrivelse(taskStepType = LagOppgaveTask.LAG_OPPGAVE,
                     maxAntallFeil = 100,
                     beskrivelse = "Lager oppgave i GoSys")
class LagOppgaveTask(private val taskRepository: TaskRepository,
                     private val oppgaveService: OppgaveService) : AsyncTaskStep {

    override fun doTask(task: Task) {
        LOG.debug("Oppretter oppgave for søknad={}", task.payload)
        oppgaveService.lagOppgave(task.payload)
    }

    override fun onCompletion(task: Task) {
        val nesteTask: Task =
                Task.nyTask(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, task.payload, task.metadata)
        taskRepository.save(nesteTask)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(LagOppgaveTask::class.java)
        const val LAG_OPPGAVE = "lagOppgave"
    }
}