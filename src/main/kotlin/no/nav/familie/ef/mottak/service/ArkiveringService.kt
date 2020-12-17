package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.mapper.ArkiverDokumentRequestMapper
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import no.nav.familie.kontrakter.felles.dokarkiv.DokarkivBruker
import no.nav.familie.kontrakter.felles.dokarkiv.IdType
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostRequest
import no.nav.familie.kontrakter.felles.dokarkiv.Sak
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ArkiveringService(private val integrasjonerClient: IntegrasjonerClient,
                        private val søknadService: SøknadService,
                        private val vedleggRepository: VedleggRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun journalførSøknad(søknadId: String): String {
        val soknad: Soknad = søknadService.get(søknadId)
        val vedlegg = vedleggRepository.findBySøknadId(soknad.id)
        val journalpostId: String = send(soknad, vedlegg)
        val søknadMedJournalpostId = soknad.copy(journalpostId = journalpostId)
        søknadService.lagreSøknad(søknadMedJournalpostId)
        return journalpostId
    }

    fun ferdigstillJournalpost(søknadId: String) {
        val soknad: Soknad = søknadService.get(søknadId)
        val journalpostId: String = soknad.journalpostId ?: error("Søknad=$søknadId mangler journalpostId")

        val enheter = integrasjonerClient.finnBehandlendeEnhet(soknad.fnr)
        if (enheter.size > 1) {
            logger.warn("Fant mer enn 1 enhet for $søknadId: $enheter")
        }
        val journalførendeEnhet = enheter.firstOrNull()?.enhetId
                                  ?: error("Ingen behandlende enhet funnet for søknad=${søknadId} ")

        integrasjonerClient.ferdigstillJournalpost(journalpostId, journalførendeEnhet)
    }

    fun oppdaterJournalpost(søknadId: String) {
        val soknad: Soknad = søknadService.get(søknadId)
        val journalpostId: String = soknad.journalpostId ?: error("Søknad=$søknadId mangler journalpostId")
        val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
        val infotrygdSaksnummer = integrasjonerClient.finnInfotrygdSaksnummerForSak(soknad.saksnummer, FAGOMRÅDE_ENSLIG_FORSØRGER, soknad.fnr)
        logger.info("Fant infotrygdsak med saksnummer=$infotrygdSaksnummer for søknad=$søknadId")

        val oppdatertJournalpost = OppdaterJournalpostRequest(
                bruker = journalpost.bruker?.let {
                    DokarkivBruker(idType = IdType.valueOf(it.type.toString()), id = it.id)
                },
                sak = Sak(fagsakId = infotrygdSaksnummer,
                          fagsaksystem = INFOTRYGD,
                          sakstype = "FAGSAK"),
                tema = journalpost.tema,
        )

        integrasjonerClient.oppdaterJournalpost(oppdatertJournalpost, journalpostId)
    }

    private fun send(soknad: Soknad, vedlegg: List<Vedlegg>): String {
        val arkiverDokumentRequest = ArkiverDokumentRequestMapper.toDto(soknad, vedlegg)
        val dokumentResponse = integrasjonerClient.arkiver(arkiverDokumentRequest)
        return dokumentResponse.journalpostId
    }
}
