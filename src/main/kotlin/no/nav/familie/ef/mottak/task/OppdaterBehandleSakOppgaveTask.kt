package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.FAGOMRÅDE_ENSLIG_FORSØRGER
import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = OppdaterBehandleSakOppgaveTask.TYPE,
                     beskrivelse = "Oppdater behandle sak oppgave i GoSys")
class OppdaterBehandleSakOppgaveTask(private val oppgaveService: OppgaveService,
                                     private val søknadService: SøknadService,
                                     private val integrasjonerClient: IntegrasjonerClient,
                                     private val taskRepository: TaskRepository) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val søknad: Søknad = søknadService.get(task.payload)
        val oppgaveId: String? = task.metadata[LagBehandleSakOppgaveTask.behandleSakOppgaveIdKey] as String?
        søknad.saksnummer?.trim()?.let { saksnummer ->
            oppgaveId?.let {
                val infotrygdSaksnummer = saksnummer.trim().let {
                    integrasjonerClient.finnInfotrygdSaksnummerForSak(saksnummer, FAGOMRÅDE_ENSLIG_FORSØRGER, søknad.fnr)
                }
                oppgaveService.oppdaterOppgave(it.toLong(), saksnummer, infotrygdSaksnummer, "familie-ef-sak-blankett")
            } ?: error("Kan ikke oppdatere oppgave uten oppgaveId")

        } ?: error("Kan ikke oppdatere behandle-sak-oppgave ettersom søknad=${søknad.id} mangler saksnummer")
    }

    override fun onCompletion(task: Task) {
        val nesteTask = Task(TYPE.nesteHovedflytTask(), task.payload, task.metadata)
        taskRepository.save(nesteTask)
    }

    companion object {
        const val TYPE = "oppdaterBehandleSakOppgave"
    }
}
