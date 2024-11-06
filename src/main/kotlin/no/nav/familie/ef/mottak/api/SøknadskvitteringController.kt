package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.service.SøknadskvitteringService
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Profile("!prod")
@RequestMapping("api/soknadskvittering", produces = [APPLICATION_JSON_VALUE])
@Unprotected
class SøknadskvitteringController(
    val søknadKvitteringService: SøknadskvitteringService,
) {
    @Unprotected
    @GetMapping("{søknadId}")
    fun hentSøknad(
        @PathVariable søknadId: String,
    ): Map<String, Any> = søknadKvitteringService.hentSøknadOgMapTilGenereltFormat(søknadId)

    @GetMapping("skrivSistePdfTilFil")
    @Unprotected
    fun skrivSistePdfTilFil(): String = søknadKvitteringService.skrivSistePdfTilFil()
}
