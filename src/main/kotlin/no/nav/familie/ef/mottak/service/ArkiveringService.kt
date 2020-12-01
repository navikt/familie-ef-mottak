package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.mapper.ArkiverDokumentRequestMapper
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import org.springframework.stereotype.Service

@Service
class ArkiveringService(private val integrasjonerClient: IntegrasjonerClient,
                        private val søknadService: SøknadService,
                        private val vedleggRepository: VedleggRepository) {

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
        val journalpostId: String = soknad.journalpostId ?: error("Søknad mangler journalpostId")
        integrasjonerClient.ferdigstillJournalpost(journalpostId)
    }

    private fun send(soknad: Soknad, vedlegg: List<Vedlegg>): String {
        val arkiverDokumentRequest = ArkiverDokumentRequestMapper.toDto(soknad, vedlegg)
        val dokumentResponse = integrasjonerClient.arkiver(arkiverDokumentRequest)
        return dokumentResponse.journalpostId
    }
}
