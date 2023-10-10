package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.mapper.ArkiverDokumentRequestMapper
import no.nav.familie.ef.mottak.repository.EttersendingVedleggRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.journalpost.Bruker
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.JournalposterForBrukerRequest
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException

@Service
class ArkiveringService(
    private val integrasjonerClient: IntegrasjonerClient,
    private val søknadService: SøknadService,
    private val ettersendingService: EttersendingService,
    private val vedleggRepository: VedleggRepository,
    private val ettersendingVedleggRepository: EttersendingVedleggRepository,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun journalførSøknad(søknadId: String, callId: String): String {
        logger.info("Henter ut søknad")
        val søknad: Søknad = søknadService.get(søknadId)
        logger.info("Henter ut vedlegg")
        val vedlegg = vedleggRepository.findBySøknadId(søknad.id)
        logger.info("Journalfører søknad med vedlegg")
        val journalpostId: String = håndterJournalpostAlleredeOpprettet(søknad.fnr, callId) {
            send(søknad, vedlegg)
        }

        val søknadMedJournalpostId = søknad.copy(journalpostId = journalpostId)
        logger.info("Oppdaterer søknad med journalpostId")
        søknadService.oppdaterSøknad(søknadMedJournalpostId)
        return journalpostId
    }

    fun journalførEttersending(ettersendingId: String, callId: String): String {
        val ettersending: Ettersending = ettersendingService.hentEttersending(ettersendingId)
        val vedlegg = ettersendingVedleggRepository.findByEttersendingId(ettersending.id)
        val journalpostId: String = håndterJournalpostAlleredeOpprettet(ettersending.fnr, callId) {
            sendEttersending(ettersending, vedlegg)
        }
        val ettersendingMedJournalpostId = ettersending.copy(journalpostId = journalpostId)
        ettersendingService.oppdaterEttersending(ettersendingMedJournalpostId)
        return journalpostId
    }

    fun ferdigstillJournalpost(søknadId: String) {
        val søknad: Søknad = søknadService.get(søknadId)
        val journalpostId: String = søknad.journalpostId ?: error("Søknad=$søknadId mangler journalpostId")

        val enheter = integrasjonerClient.finnBehandlendeEnhet(søknad.fnr)
        if (enheter.size > 1) {
            logger.warn("Fant mer enn 1 enhet for $søknadId: $enheter")
        }
        val journalførendeEnhet = enheter.firstOrNull()?.enhetId
            ?: error("Ingen behandlende enhet funnet for søknad=$søknadId ")

        integrasjonerClient.ferdigstillJournalpost(journalpostId, journalførendeEnhet)
    }

    private fun send(søknad: Søknad, vedlegg: List<Vedlegg>): String {
        val arkiverDokumentRequest = ArkiverDokumentRequestMapper.toDto(søknad, vedlegg)
        val dokumentResponse = integrasjonerClient.arkiver(arkiverDokumentRequest)
        return dokumentResponse.journalpostId
    }

    private fun sendEttersending(
        ettersending: Ettersending,
        vedlegg: List<EttersendingVedlegg>,
    ): String {
        val arkiverDokumentRequest = ArkiverDokumentRequestMapper.fromEttersending(ettersending, vedlegg)
        val dokumentResponse = integrasjonerClient.arkiver(arkiverDokumentRequest)
        return dokumentResponse.journalpostId
    }

    fun hentJournalpostIdForBrukerOgEksternReferanseId(eksternReferanseId: String, fnr: String): Journalpost? {
        val request = JournalposterForBrukerRequest(
            brukerId = Bruker(id = fnr, type = BrukerIdType.FNR),
            antall = 1000,
            tema = listOf(Tema.ENF),
            journalposttype = listOf(Journalposttype.I),
        )
        val journalposterForBruker =
            integrasjonerClient.hentJournalposterForBruker(journalpostForBrukerRequest = request)

        logger.info("Antall journalposter for bruker: ${journalposterForBruker.size}")
        journalposterForBruker.forEach {
            logger.info(
                "journalpostId: ${it.journalpostId}" +
                    " eksternReferanseId: ${it.eksternReferanseId} " +
                    " mottatt: ${it.datoMottatt}" +
                    " status: ${it.journalstatus} ",
            )
        }
        return journalposterForBruker.find { it.eksternReferanseId == eksternReferanseId }
    }

    private fun håndterJournalpostAlleredeOpprettet(fnr: String, callId: String, journalfør: () -> String): String {
        return try {
            journalfør()
        } catch (e: RessursException) {
            if (e.cause is HttpClientErrorException.Conflict) {
                logger.warn("409 conflict for eksternReferanseId ved journalføring med callId: $callId.")
                hentJournalpostIdForBrukerOgEksternReferanseId(
                    callId,
                    fnr,
                )?.journalpostId
                    ?: error("Fant ikke journalpost for callId (eksternReferanseId) $callId")
            } else {
                throw e
            }
        }
    }
}
