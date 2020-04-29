package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.integration.PdfClient
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.søknad.SkjemaForArbeidssøker
import no.nav.familie.kontrakter.ef.søknad.Søknad
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PdfService(private val soknadRepository: SoknadRepository, private val pdfClient: PdfClient) {

    fun lagPdf(id: String) {

        val innsending = soknadRepository.findByIdOrNull(id) ?: error("Kunne ikke finne søknad ($id) i database")
        val feltMap = lagFeltMap(innsending)
        val søknadPdf = pdfClient.lagPdf(feltMap)
        val oppdatertSoknad = innsending.copy(søknadPdf = søknadPdf)
        soknadRepository.saveAndFlush(oppdatertSoknad)
    }

    private fun lagFeltMap(innsending: Soknad): Map<String, Any> {
        return if (innsending.dokumenttype == DOKUMENTTYPE_OVERGANGSSTØNAD) {
            val dto = SøknadMapper.toDto<Søknad>(innsending)
            SøknadTreeWalker.mapSøknadsfelter(dto)
        } else if (innsending.dokumenttype == DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER) {
            val dto = SøknadMapper.toDto<SkjemaForArbeidssøker>(innsending)
            SøknadTreeWalker.mapSkjemafelter(dto)
        } else {
            error("Ukjent eller manglende dokumenttype id: ${innsending.id}")
        }
    }
}
