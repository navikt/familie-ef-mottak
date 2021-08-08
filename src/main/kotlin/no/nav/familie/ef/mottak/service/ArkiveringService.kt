package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.mapper.ArkiverDokumentRequestMapper
import no.nav.familie.ef.mottak.mapper.EttersendingMapper
import no.nav.familie.ef.mottak.repository.EttersendingVedleggRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import no.nav.familie.kontrakter.ef.ettersending.EttersendingDto
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.DokarkivBruker
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostRequest
import no.nav.familie.kontrakter.felles.dokarkiv.Sak
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ArkiveringService(private val integrasjonerClient: IntegrasjonerClient,
                        private val søknadService: SøknadService,
                        private val ettersendingService: EttersendingService,
                        private val vedleggRepository: VedleggRepository,
                        private val ettersendingVedleggRepository: EttersendingVedleggRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun journalførSøknad(søknadId: String): String {
        val søknad: Søknad = søknadService.get(søknadId)
        val vedlegg = vedleggRepository.findBySøknadId(søknad.id)
        val journalpostId: String = send(søknad, vedlegg)
        val søknadMedJournalpostId = søknad.copy(journalpostId = journalpostId)
        søknadService.lagreSøknad(søknadMedJournalpostId)
        return journalpostId
    }

    fun journalførEttersending(ettersendingId: String): String {
        val ettersending: Ettersending = ettersendingService.hentEttersending(ettersendingId)
        val søknadFørEttersending = hentSøknadForEttersending(ettersending)
        val vedlegg = ettersendingVedleggRepository.findByEttersendingId(ettersending.id)
        val journalpostId: String = sendEttersending(ettersending, vedlegg, søknadFørEttersending)
        val ettersendingMedJournalpostId = ettersending.copy(journalpostId = journalpostId)
        ettersendingService.lagreEttersending(ettersendingMedJournalpostId)
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
                                  ?: error("Ingen behandlende enhet funnet for søknad=${søknadId} ")

        integrasjonerClient.ferdigstillJournalpost(journalpostId, journalførendeEnhet)
    }

    fun oppdaterJournalpost(søknadId: String) {
        val søknad: Søknad = søknadService.get(søknadId)
        val journalpostId: String = søknad.journalpostId ?: error("Søknad=$søknadId mangler journalpostId")
        val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
        val infotrygdSaksnummer = søknad.saksnummer?.trim()?.let {
            integrasjonerClient.finnInfotrygdSaksnummerForSak(it, FAGOMRÅDE_ENSLIG_FORSØRGER, søknad.fnr)
        } ?: error("Søknaden mangler saksnummer - kan ikke finne infotrygdsak for søknad=$søknadId")

        logger.info("Fant infotrygdsak med saksnummer=$infotrygdSaksnummer for søknad=$søknadId")

        val oppdatertJournalpost = OppdaterJournalpostRequest(
                bruker = journalpost.bruker?.let {
                    DokarkivBruker(idType = BrukerIdType.valueOf(it.type.toString()), id = it.id)
                },
                sak = Sak(fagsakId = infotrygdSaksnummer,
                          fagsaksystem = Fagsystem.IT01,
                          sakstype = "FAGSAK"),
                tema = journalpost.tema?.let { Tema.valueOf(it) },
        )

        integrasjonerClient.oppdaterJournalpost(oppdatertJournalpost, journalpostId)
    }

    private fun send(søknad: Søknad, vedlegg: List<Vedlegg>): String {
        val arkiverDokumentRequest = ArkiverDokumentRequestMapper.toDto(søknad, vedlegg)
        val dokumentResponse = integrasjonerClient.arkiver(arkiverDokumentRequest)
        return dokumentResponse.journalpostId
    }

    private fun sendEttersending(ettersending: Ettersending,
                                 vedlegg: List<EttersendingVedlegg>,
                                 søknadFørEttersending: Søknad? = null): String {
        val arkiverDokumentRequest = ArkiverDokumentRequestMapper.fromEttersending(ettersending, vedlegg, søknadFørEttersending)
        val dokumentResponse = integrasjonerClient.arkiver(arkiverDokumentRequest)
        return dokumentResponse.journalpostId
    }

    private fun hentSøknadForEttersending(ettersending: Ettersending) =
            EttersendingMapper.toDto<EttersendingDto>(ettersending).ettersendingForSøknad?.let { søknadService.get(it.søknadId) }
}
