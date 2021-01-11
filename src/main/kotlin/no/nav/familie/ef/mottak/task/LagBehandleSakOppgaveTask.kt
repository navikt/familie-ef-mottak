package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.service.FAGOMRÅDE_ENSLIG_FORSØRGER
import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = LagBehandleSakOppgaveTask.TYPE,
                     beskrivelse = "Lager behandle sak oppgave i GoSys")
class LagBehandleSakOppgaveTask(private val oppgaveService: OppgaveService,
                                private val søknadService: SøknadService,
                                private val integrasjonerClient: IntegrasjonerClient) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val soknad: Soknad = søknadService.get(task.payload)
        val journalpostId: String = soknad.journalpostId ?: error("Søknad mangler journalpostId")
        val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
        soknad.saksnummer?.let {
            val infotrygdSaksnummer = it.trim().let {
                integrasjonerClient.finnInfotrygdSaksnummerForSak(it, FAGOMRÅDE_ENSLIG_FORSØRGER, soknad.fnr)
            }
            oppgaveService.lagBehandleSakOppgave(journalpost, it, infotrygdSaksnummer)
        } ?: error("Kan ikke opprette behandle-sak-oppgave ettersom søknad=${soknad.id} mangler saksnummer")
    }

    override fun onCompletion(task: Task) {
    }

    companion object {
        const val TYPE = "lagBehandleSakOppgave"
    }
}
