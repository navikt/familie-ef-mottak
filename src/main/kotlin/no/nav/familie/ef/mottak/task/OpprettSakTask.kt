package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.service.SakService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = OpprettSakTask.TYPE,
                     beskrivelse = "Oppretter sak i Infotrygd")
class OpprettSakTask(private val taskRepository: TaskRepository,
                     private val sakService: SakService,
                     private val soknadRepository: SoknadRepository) : AsyncTaskStep {


    override fun doTask(task: Task) {
        val oppgaveId: Long? = task.metadata[LagBehandleSakOppgaveTask.behandleSakOppgaveIdKey] as Long?
        oppgaveId?.let {
            val sakId = sakService.opprettSak(task.payload, it.toString())
            val soknad = soknadRepository.findByIdOrNull(task.payload) ?: error("Søknad har forsvunnet!")
            val soknadMedSaksnummer = soknad.copy(saksnummer = sakId)
            soknadRepository.save(soknadMedSaksnummer)
        } ?: error("Fant ikke oppgaveId for behandle-sak-oppgave i tasken")


    }

    override fun onCompletion(task: Task) {
        val nesteTask = Task(OppdaterBehandleSakOppgaveTask.TYPE, task.payload, task.metadata)
        taskRepository.save(nesteTask)
    }

    companion object {

        const val TYPE = "opprettSak"
    }
}
