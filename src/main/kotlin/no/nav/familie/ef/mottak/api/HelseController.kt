package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.integration.ITextPdfClient
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Profile("!prod")
@RestController
@RequestMapping(path = ["/api/helsesjekk"])
@Unprotected
class HelseController (val iTextPdfClient: ITextPdfClient){
    @GetMapping
    fun helsesjekk(): String {
        return iTextPdfClient.helsesjekk()
    }
}
