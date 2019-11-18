package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.repository.domain.Henvendelse
import no.nav.familie.ef.mottak.service.MottakServiceImpl
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/api/soknad"], produces = [APPLICATION_JSON_VALUE])
@Protected
class SøknadController(val mottakServiceImpl: MottakServiceImpl) {

    @PostMapping
    fun sendInn(@RequestBody søknadDto: String): Kvittering {
        return mottakServiceImpl.motta(søknadDto)
    }
    @GetMapping("{id}")
    fun get(@PathVariable id: Long): Henvendelse {
        return mottakServiceImpl.get(id)
    }
}
