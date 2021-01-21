package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.service.SakService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = OpprettSakTask.TYPE,
                     beskrivelse = "Oppretter sak i Infotrygd")
class OpprettSakTask(private val taskRepository: TaskRepository,
                     private val sakService: SakService,
                     private val soknadRepository: SoknadRepository) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val oppgaveId: String? = task.metadata[LagBehandleSakOppgaveTask.behandleSakOppgaveIdKey] as String?
        oppgaveId?.let {
            val sakId = sakService.opprettSak(task.payload, it)?.trim()
            val soknad = soknadRepository.findByIdOrNull(task.payload) ?: error("Søknad har forsvunnet!")
            val soknadMedSaksnummer = soknad.copy(saksnummer = sakId)
            soknadRepository.save(soknadMedSaksnummer)
        } ?: error("Fant ikke oppgaveId for behandle-sak-oppgave i tasken")


    }

    override fun onCompletion(task: Task) {
        val soknad = soknadRepository.findByIdOrNull(task.payload)
        val nesteTask = if (soknad?.saksnummer != null) {
            Task(OppdaterBehandleSakOppgaveTask.TYPE, task.payload, task.metadata)
        } else {
            logger.warn("Det er allerede opprettet en sak for denne oppgaven - trolig gjort manuelt av saksbehandler")
            Task(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, task.payload, task.metadata)
        }
        taskRepository.save(nesteTask)
    }

    companion object {

        const val TYPE = "opprettSak"
    }
}
