package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.api.dto.SøknadDto
import no.nav.familie.ef.mottak.api.exception.SøknadNotFoundException
import no.nav.familie.ef.mottak.repository.SøknadDAO
import no.nav.familie.ef.mottak.repository.domain.Søknad
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/api/soknad"], produces = [APPLICATION_JSON_VALUE])
class SøknadController(private val søknadDAO: SøknadDAO) {

    @PostMapping("sendInn")
    fun sendInn(@RequestBody søknadDto: SøknadDto): Kvittering {
        val id = søknadDAO.lagreSøknad(søknadDto.søknad_json, søknadDto.fnr)
        return Kvittering("Søknad lagret - id " + id + ", fnr " + søknadDto.fnr)
    }

    @ResponseBody
    @GetMapping("{id}")
    fun get(@PathVariable id: Long): Søknad {
        return søknadDAO.hentSøknadForBruker(id) ?: throw SøknadNotFoundException()
    }

    /*
    @PostMapping("sendInn")
    fun sendInn(@RequestBody søknadDto: String): Kvittering {
        return mottakServiceImpl.motta(søknadDto)
    }
    @GetMapping("{id}")
    fun get(@PathVariable id: Long): Henvendelse {
        return mottakServiceImpl.get(id)
    }
    */
}
