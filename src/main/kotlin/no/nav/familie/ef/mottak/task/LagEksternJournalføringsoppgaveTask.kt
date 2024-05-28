package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.hendelse.skalBehandles
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(
    taskStepType = LagEksternJournalføringsoppgaveTask.TYPE,
    beskrivelse = "Lager oppgave i GoSys",
)
class LagEksternJournalføringsoppgaveTask(
    private val ettersendingRepository: EttersendingRepository,
    private val oppgaveService: OppgaveService,
    private val søknadRepository: SøknadRepository,
    private val journalpostClient: IntegrasjonerClient,
) : AsyncTaskStep {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val journalpostId = task.payload

        val journalpost = journalpostClient.hentJournalpost(journalpostId)

        // Ved helt spesielle race conditions kan man tenke seg at vi fikk opprettet
        // denne (LagEksternJournalføringsoppgaveTask) før søknaden fikk en journalpostId. Gjelder særlig dersom
        // arkiveringService timer ut, men kallet går igjennom.
        if (!journalpost.skalBehandles()) {
            logger.info("Journalposten er endret etter melding på kø. Lager ikke task for journalpost med id=$journalpostId")
        } else if (finnesIkkeSøknadMedJournalpostId(journalpostId) && finnesIkkeEttersendingMedJournalpostId(journalpostId)) {
            oppgaveService.lagJournalføringsoppgaveForJournalpostId(journalpostId)
        } else {
            logger.info(
                "Lager ikke oppgave for journalpostId=$journalpostId da denne allerede håndteres av normal journalføringsløype i mottak",
            )
        }
    }

    private fun finnesIkkeSøknadMedJournalpostId(journalpostId: String) = søknadRepository.findByJournalpostId(journalpostId) == null

    private fun finnesIkkeEttersendingMedJournalpostId(journalpostId: String) =
        ettersendingRepository.findByJournalpostId(journalpostId) == null

    companion object {
        const val TYPE = "lagEksternJournalføringsoppgave"
    }
}
