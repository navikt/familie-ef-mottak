package no.nav.familie.ef.mottak.api

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.repository.SøknadDAO
import no.nav.familie.ef.mottak.repository.domain.Søknad
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/api/soknad"], produces = [APPLICATION_JSON_VALUE])
class SøknadController(private val søknadDAO: SøknadDAO) {

    @PostMapping("sendInn")
    fun sendInn(@RequestBody søknadDto: SøknadDto): Kvittering {
        søknadDAO.lagreSøknad(søknadDto.id, søknadDto.toString())
        return Kvittering("Søknad lagret med id " + søknadDto.id)
    }

    @ResponseBody
    @GetMapping("{id}")
    fun get(@PathVariable id: Long): Søknad? {
        return søknadDAO.hentSøknadForBruker(id)
    }

    data class SøknadDto(
            val id: Long,
            val søknad_json: String
    )
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
