package no.nav.familie.ef.mottak.mockapi

import no.nav.familie.ef.mottak.repository.domain.Fil
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping(path = ["/testapi/soknad/"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Unprotected
class SøknadControllerExtension(val søknadService: SøknadService) {

    @GetMapping("{id}")
    fun get(@PathVariable id: String): Soknad {
        return søknadService.get(id)
    }

    @GetMapping("{id}/pdf")
    fun getPdf(@PathVariable id: String): Fil? {
        return søknadService.get(id).søknadPdf
    }
}