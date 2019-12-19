package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.api.dto.SøknadDto
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/api/soknad"], produces = [APPLICATION_JSON_VALUE])
@Protected
class SøknadController(val søknadService: SøknadService) {

    @PostMapping
    fun sendInn(@RequestBody søknadDto: SøknadDto): Kvittering {
        return søknadService.motta(søknadDto)
    }

    @GetMapping("{id}")
    fun get(@PathVariable id: String): Soknad {
        return søknadService.get(id)
    }
}
