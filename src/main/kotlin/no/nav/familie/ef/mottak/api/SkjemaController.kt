package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.kontrakter.ef.søknad.SkjemaForArbeidssøker
import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/skjema"], produces = [APPLICATION_JSON_VALUE])
@ProtectedWithClaims(
    issuer = EksternBrukerUtils.ISSUER_TOKENX,
    claimMap = ["acr=Level4"]
)
class SkjemaController(val søknadService: SøknadService) {

    @PostMapping
    fun sendInn(@RequestBody skjemaForArbeidssøker: SkjemaForArbeidssøker): Kvittering {
        return søknadService.motta(skjemaForArbeidssøker)
    }
}
