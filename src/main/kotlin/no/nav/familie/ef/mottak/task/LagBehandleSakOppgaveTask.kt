package no.nav.familie.ef.mottak.task

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.ef.mottak.service.SakService
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = LagBehandleSakOppgaveTask.TYPE,
                     beskrivelse = "Lager behandle sak oppgave i GoSys")
class LagBehandleSakOppgaveTask(private val oppgaveService: OppgaveService,
                                private val søknadService: SøknadService,
                                private val integrasjonerClient: IntegrasjonerClient,
                                private val sakService: SakService,
                                private val taskRepository: TaskRepository) : AsyncTaskStep {

    val antallJournalposterAutomatiskBehandlet: Counter = Metrics.counter("alene.med.barn.journalposter.automatisk.behandlet")
    val antallJournalposterManueltBehandlet: Counter = Metrics.counter("alene.med.barn.journalposter.manuelt.behandlet")

    override fun doTask(task: Task) {
        val soknad: Soknad = søknadService.get(task.payload)
        val journalpostId: String = soknad.journalpostId ?: error("Søknad mangler journalpostId")
        val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
        if (sakService.kanOppretteInfotrygdSak(soknad)) {
            val lagBehandleSakOppgave = oppgaveService.lagBehandleSakOppgave(journalpost)
            task.metadata.apply {
                this[behandleSakOppgaveIdKey] = lagBehandleSakOppgave.toString()
            }
                taskRepository.save(task)
        }
    }

    override fun onCompletion(task: Task) {

        val nesteTask = if (task.metadata[behandleSakOppgaveIdKey] == null) {
            antallJournalposterManueltBehandlet.increment()
            Task(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, task.payload, task.metadata)
        } else {
            antallJournalposterAutomatiskBehandlet.increment()
            Task(OpprettSakTask.TYPE, task.payload, task.metadata)
        }

        taskRepository.save(nesteTask)

    }

    companion object {

        const val behandleSakOppgaveIdKey = "behandleSakOppgaveId"
        const val TYPE = "lagBehandleSakOppgave"
    }
}
