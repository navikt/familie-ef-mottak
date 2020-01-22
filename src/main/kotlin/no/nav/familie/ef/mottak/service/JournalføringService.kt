package no.nav.familie.ef.mottak.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.integration.ArkivClient
import no.nav.familie.ef.mottak.mapper.ArkiverDokumentRequestMapper
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.søknad.Søknad
import no.nav.familie.kontrakter.felles.objectMapper
import org.springframework.stereotype.Service

@Service
class JournalføringService(private val arkivClient: ArkivClient,
                           private val søknadService: SøknadService) {

    fun journalførSøknad(søknadId: String) {
        val soknad: Soknad = søknadService.get(søknadId)
        val søknad = objectMapper.readValue<Søknad>(soknad.søknadJson)
        val journalpostId: String = send(søknad)
        val søknadMedJournalpostId = soknad.copy(journalpostId = journalpostId)
        søknadService.lagreSøknad(søknadMedJournalpostId)
    }

    private fun send(soknad: Søknad): String {
        val arkiverDokumentRequest = ArkiverDokumentRequestMapper.toDto(soknad)
        val ressurs = arkivClient.arkiver(arkiverDokumentRequest)
        return ressurs.journalpostId
    }
}