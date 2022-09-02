package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.SaksbehandlingClient
import no.nav.familie.kontrakter.ef.journalføring.AutomatiskJournalføringRequest
import no.nav.familie.kontrakter.ef.journalføring.AutomatiskJournalføringResponse
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AutomatiskJournalføringService(
    val saksbehandlingClient: SaksbehandlingClient,
    val søknadService: SøknadService,
    val taskRepository: TaskRepository
) {

    @Transactional
    fun lagFørstegangsbehandlingOgBehandleSakOppgave(
        personIdent: String,
        journalpostId: String,
        stønadstype: StønadType
    ): AutomatiskJournalføringResponse {
        val arkiverDokumentRequest = AutomatiskJournalføringRequest(
            personIdent = personIdent,
            journalpostId = journalpostId,
            stønadstype = stønadstype
        )
        return saksbehandlingClient.lagFørstegangsbehandlingOgBehandleSakOppgave(arkiverDokumentRequest)
    }
}
