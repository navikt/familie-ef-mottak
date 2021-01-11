package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Soknad
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
        val soknad: Soknad = søknadService.get(task.payload)
        val oppgaveId: Long? = task.metadata[LagBehandleSakOppgaveTask.behandleSakOppgaveIdKey] as Long?
        soknad.saksnummer?.let { saksnummer ->
            oppgaveId?.let {
                val infotrygdSaksnummer = saksnummer.trim().let {
                    integrasjonerClient.finnInfotrygdSaksnummerForSak(saksnummer, FAGOMRÅDE_ENSLIG_FORSØRGER, soknad.fnr)
                }
                oppgaveService.oppdaterOppgave(it, saksnummer, infotrygdSaksnummer)
            } ?: error("Kan ikke oppdatere oppgave uten oppgaveId")

        } ?: error("Kan ikke oppdatere behandle-sak-oppgave ettersom søknad=${soknad.id} mangler saksnummer")
    }

    override fun onCompletion(task: Task) {
        val nesteTask = Task(OppdaterJournalføringTask.TYPE, task.payload, task.metadata)
        taskRepository.save(nesteTask)
    }

    companion object {

        const val TYPE = "oppdaterBehandleSakOppgave"
    }
}
