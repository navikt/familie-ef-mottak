package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.SaksbehandlingClient
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.util.UtledPrioritetForSøknadUtil
import no.nav.familie.kontrakter.ef.journalføring.AutomatiskJournalføringRequest
import no.nav.familie.kontrakter.ef.journalføring.AutomatiskJournalføringResponse
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AutomatiskJournalføringService(
    val saksbehandlingClient: SaksbehandlingClient,
    val søknadService: SøknadService,
    val taskService: TaskService,
) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun journalførAutomatisk(
        personIdent: String,
        journalpostId: String,
        stønadstype: StønadType,
        mappeId: Long?,
        søknad: Søknad,
    ): Boolean {
        val arkiverDokumentRequest =
            AutomatiskJournalføringRequest(
                personIdent = personIdent,
                journalpostId = journalpostId,
                stønadstype = stønadstype,
                mappeId = mappeId,
                prioritet = UtledPrioritetForSøknadUtil.utledPrioritet(søknad),
            )
        try {
            val respons =
                saksbehandlingClient.journalførAutomatisk(arkiverDokumentRequest)
            infoLog(journalpostId, respons)
        } catch (e: Exception) {
            logger.error("Feil ved prosessering av automatisk journalhendelser for $stønadstype: journalpostId: $journalpostId")
            secureLogger.error("Feil ved prosessering av automatisk journalhendelser", e)
            return false
        }
        return true
    }

    private fun infoLog(
        journalpostId: String,
        automatiskJournalføringResponse: AutomatiskJournalføringResponse,
    ) {
        logger.info(
            "Automatisk journalført:$journalpostId: " +
                "behandlingId: ${automatiskJournalføringResponse.behandlingId}, " +
                "fagsakId: ${automatiskJournalføringResponse.fagsakId} ",
        )
    }
}
