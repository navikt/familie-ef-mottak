package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.kontrakter.ef.ettersending.SøknadMedDokumentasjonsbehovDto
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(consumes = [APPLICATION_JSON_VALUE], path = ["/api/person"], produces = [APPLICATION_JSON_VALUE])
@ProtectedWithClaims(
    issuer = EksternBrukerUtils.ISSUER_TOKENX,
    claimMap = ["acr=Level4"],
)
class PersonController(
    val søknadService: SøknadService,
) {
    @PostMapping("soknader")
    fun søknaderForPerson(
        @RequestBody personIdent: PersonIdent,
    ): ResponseEntity<List<SøknadMedDokumentasjonsbehovDto>> {
        if (!EksternBrukerUtils.personIdentErLikInnloggetBruker(personIdent.ident)) {
            throw ApiFeil("Fnr fra token matcher ikke fnr i request", HttpStatus.FORBIDDEN)
        }
        return ResponseEntity.ok().body(søknadService.hentDokumentasjonsbehovForPerson(personIdent.ident))
    }
}
