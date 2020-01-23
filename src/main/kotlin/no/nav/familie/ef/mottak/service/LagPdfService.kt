package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.PdfClient
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.kontrakter.ef.søknad.Felt
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class LagPdfService(private val soknadRepository: SoknadRepository, private val pdfClient: PdfClient) {

    fun lagPdf(id: String) {

        val soknad = soknadRepository.findByIdOrNull(id) ?: error("Kunne ikke finne søknad ($id) i datatabse")

        val kontraktssøknad = SøknadMapper.toDto(soknad)
        val list: List<Felt<*>> = SøknadTreeWalker.finnFelter(kontraktssøknad)
        // val labelValueSøknadsJson = objectMapper.writeValueAsString(list)

        soknad.copy(søknadPdf = pdfClient.lagPdf(list))
        soknadRepository.saveAndFlush(soknad)
    }


}
