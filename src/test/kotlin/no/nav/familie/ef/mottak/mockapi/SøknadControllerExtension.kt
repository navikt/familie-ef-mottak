package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.mockapi

import no.nav.familie.ef.mottak.api.SøknadController
import no.nav.familie.ef.mottak.repository.domain.Soknad
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@GetMapping("{id}")
fun SøknadController.get(@PathVariable id: String): Soknad {
    return søknadService.get(id)
}
