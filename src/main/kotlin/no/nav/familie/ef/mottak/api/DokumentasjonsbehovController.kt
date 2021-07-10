package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.kontrakter.ef.søknad.dokumentasjonsbehov.DokumentasjonsbehovDto
import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(path = ["/api/soknad/dokumentasjonsbehov"], produces = [APPLICATION_JSON_VALUE])
@RequiredIssuers(
        ProtectedWithClaims(issuer = "selvbetjening", claimMap = ["acr=Level4"]),
        ProtectedWithClaims(issuer = "tokenx", claimMap = ["acr=Level4"])
)
class DokumentasjonsbehovController(val søknadService: SøknadService) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @GetMapping("{søknadId}")
    fun hentDokumentasjonsbehov(@PathVariable("søknadId") søknadId: UUID): ResponseEntity<DokumentasjonsbehovDto> {
        val dokumentasjonsbehov = søknadService.hentDokumentasjonsbehovForSøknad(søknadId)
        val fnrFraToken = EksternBrukerUtils.hentFnrFraToken()
        if (fnrFraToken != dokumentasjonsbehov.personIdent) {
            logger.warn("Fødselsnummer fra token matcher ikke fnr på søknaden")
            secureLogger.info("TokenFnr={} matcher ikke søknadFnr={} søknadId={}",
                              fnrFraToken,
                              dokumentasjonsbehov.personIdent,
                              søknadId)
            throw ApiFeil("Fnr fra token matcher ikke fnr på søknaden", HttpStatus.FORBIDDEN)
        }
        return ResponseEntity.ok(dokumentasjonsbehov)
    }

}
