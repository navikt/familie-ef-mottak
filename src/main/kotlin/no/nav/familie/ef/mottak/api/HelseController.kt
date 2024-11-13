package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.integration.ITextPdfClient
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Profile("!prod")
@RestController
@RequestMapping(path = ["/api"])
@Unprotected
class HelseController(
    val iTextPdfClient: ITextPdfClient,
) {
    @GetMapping("/helsesjekk")
    fun testerPdfOk(): String = iTextPdfClient.helsesjekk()

    @GetMapping("/mottak/helsesjekk")
    fun girSvarTilPdf(): String = "OK - mottak"
}
