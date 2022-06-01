package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.service.EttersendingService
import no.nav.familie.ef.mottak.util.okEllerKastException
import no.nav.familie.kontrakter.ef.ettersending.EttersendelseDto
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.familie.sikkerhet.EksternBrukerUtils.personIdentErLikInnloggetBruker
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/ettersending"], consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
@ProtectedWithClaims(
    issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"]
)
class EttersendingController(val ettersendingService: EttersendingService) {

    @PostMapping
    fun ettersend(@RequestBody ettersending: Map<StønadType, EttersendelseDto>): Kvittering {
        return okEllerKastException { ettersendingService.mottaEttersending(ettersending) }
    }

    @PostMapping("person")
    fun hentForPerson(@RequestBody personIdent: PersonIdent): ResponseEntity<List<EttersendelseDto>> {
        if (!personIdentErLikInnloggetBruker(personIdent.ident)) {
            throw ApiFeil("Fnr fra token matcher ikke fnr i request", HttpStatus.FORBIDDEN)
        }
        val ettersendingData = ettersendingService.hentEttersendingsdataForPerson(personIdent)

        return ResponseEntity.ok(ettersendingData)
    }
}
