package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.PdfClient
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.repository.SoknadRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PdfService(private val soknadRepository: SoknadRepository, private val pdfClient: PdfClient) {

    fun lagPdf(id: String) {

        val soknad = soknadRepository.findByIdOrNull(id) ?: error("Kunne ikke finne søknad ($id) i datatabse")

        val kontraktssøknad = SøknadMapper.toDto(soknad)
        val søknadMap: Map<String, Any> = SøknadTreeWalker.mapSøknadsfelterTilMap(kontraktssøknad)
        val søknadPdf = pdfClient.lagPdf(søknadMap)

        val oppdatertSoknad = soknad.copy(søknadPdf = søknadPdf)
        soknadRepository.saveAndFlush(oppdatertSoknad)
    }

}
