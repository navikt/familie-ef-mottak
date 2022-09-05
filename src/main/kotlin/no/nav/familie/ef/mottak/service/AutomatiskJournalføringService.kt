package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.SaksbehandlingClient
import no.nav.familie.kontrakter.ef.journalføring.AutomatiskJournalføringRequest
import no.nav.familie.kontrakter.ef.journalføring.AutomatiskJournalføringResponse
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AutomatiskJournalføringService(
    val saksbehandlingClient: SaksbehandlingClient,
    val søknadService: SøknadService,
    val taskRepository: TaskRepository
) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @Transactional
    fun lagFørstegangsbehandlingOgBehandleSakOppgave(
        personIdent: String,
        journalpostId: String,
        stønadstype: StønadType
    ): Boolean {
        val arkiverDokumentRequest = AutomatiskJournalføringRequest(
            personIdent = personIdent,
            journalpostId = journalpostId,
            stønadstype = stønadstype
        )
        try {
            val lagFørstegangsbehandlingOgBehandleSakOppgave =
                saksbehandlingClient.lagFørstegangsbehandlingOgBehandleSakOppgave(arkiverDokumentRequest)
            infoLog(journalpostId, lagFørstegangsbehandlingOgBehandleSakOppgave)
        } catch (e: Exception) {
            logger.error("Feil ved prosessering av automatisk journalhendelser for $stønadstype: journalpostId: $journalpostId, fallback => manuell")
            secureLogger.error("Feil ved prosessering av automatisk journalhendelser", e)
            return false
        }
        return true
    }

    private fun infoLog(
        journalpostId: String,
        lagFørstegangsbehandlingOgBehandleSakOppgave: AutomatiskJournalføringResponse
    ) {
        logger.info(
            "Automatisk journalført:$journalpostId: " +
                "behandlingId: ${lagFørstegangsbehandlingOgBehandleSakOppgave.behandlingId}, " +
                "fagsakId: ${lagFørstegangsbehandlingOgBehandleSakOppgave.fagsakId}, " +
                "behandleSakOppgaveId: ${lagFørstegangsbehandlingOgBehandleSakOppgave.behandleSakOppgaveId}"
        )
    }
}
