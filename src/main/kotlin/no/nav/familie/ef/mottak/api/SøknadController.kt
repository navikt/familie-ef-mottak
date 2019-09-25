package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.log
import no.nav.familie.ef.mottak.repository.domain.Henvendelse
import no.nav.familie.ef.mottak.service.MottakServiceImpl
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.slf4j.LoggerFactory

@RestController
@RequestMapping(path = ["/api/soknad"], produces = [APPLICATION_JSON_VALUE])
class SøknadController(private val registry: MeterRegistry, val mottakServiceImpl: MottakServiceImpl) {

    private val log = LoggerFactory.getLogger(SøknadController::class.java)

    @PostMapping("sendInn")
    fun sendInn(@RequestBody søknadDto: String): Kvittering {
        return mottakServiceImpl.motta(søknadDto)
    }

    @GetMapping("{id}")
    fun get(@PathVariable id: Long): Henvendelse {
        registry.counter("familie-ef-mottak.testcounter", Tags.of("type", "fatale")).increment()
        log.info("Test info")
        log.error("Test error")
        return mottakServiceImpl.get(id)
    }

}
