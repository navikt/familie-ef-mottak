package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.service.PdfService
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Profile("!prod")
@RequestMapping("api/soknadskvittering", produces = [APPLICATION_JSON_VALUE])
@Unprotected
class SøknadskvitteringController(
    private val pdfService: PdfService,
    val søknadRepository: SøknadRepository,
    val vedleggRepository: VedleggRepository,
) {
    @Unprotected
    @GetMapping("{id}")
    fun søknad(
        @PathVariable id: String,
    ): Map<String, Any> {
        val innsending = søknadRepository.findByIdOrNull(id) ?: error("Kunne ikke finne søknad ($id) i database")
        val vedleggTitler = vedleggRepository.finnTitlerForSøknadId(id).sorted()
        return pdfService.lagFeltMap(innsending, vedleggTitler)
    }
}
