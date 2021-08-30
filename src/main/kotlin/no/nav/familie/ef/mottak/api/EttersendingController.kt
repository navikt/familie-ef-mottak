package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.service.EttersendingService
import no.nav.familie.ef.mottak.util.okEllerKastException
import no.nav.familie.kontrakter.ef.ettersending.EttersendingMedVedlegg
import no.nav.familie.kontrakter.ef.ettersending.EttersendingResponseData
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/ettersending"], consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
@Protected
class EttersendingController(val ettersendingService: EttersendingService) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @PostMapping
    fun ettersend(@RequestBody ettersending: EttersendingMedVedlegg): Kvittering {
        return okEllerKastException { ettersendingService.mottaEttersending(ettersending) }
    }

    @PostMapping("person")
    fun hentForPerson(@RequestBody personIdent: PersonIdent): ResponseEntity<List<EttersendingResponseData>> {
        val fnrFraToken = EksternBrukerUtils.hentFnrFraToken()
        if (fnrFraToken != personIdent.ident) {
            logger.warn("Fødselsnummer fra token matcher ikke fnr på søknaden")
            secureLogger.info("TokenFnr={} matcher ikke søknadFnr={} søknadId={}",
                              fnrFraToken,
                              personIdent.ident)
            throw ApiFeil("Fnr fra token matcher ikke fnr på søknaden", HttpStatus.FORBIDDEN)
        }
        val ettersendingData = ettersendingService.hentEttersendingsdataForPerson(personIdent)

        return ResponseEntity.ok(ettersendingData)
    }

}
