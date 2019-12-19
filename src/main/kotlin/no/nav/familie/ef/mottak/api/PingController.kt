package no.nav.familie.ef.mottak.api

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PingController {

    @GetMapping("/ping")
    @Unprotected
    fun ping(): String {
        return "Ack - vi har kontakt"
    }

//    @GetMapping("/azureadprotected")
//    @ProtectedWithClaims(issuer="azuread", claimMap = ["acr=Level4"]  )
//    fun azuread(): String {
//        return "Ack - vi har kontakt"
//    }
//
//    @GetMapping("/selvbetjening")
//    @ProtectedWithClaims(issuer="selvbetjening", claimMap = ["acr=Level4"]  )
//    fun pong(): String {
//        return "Ack - vi har kontakt"
//    }
//
//    @GetMapping("/protected")
//    @Protected
//    fun pang(): String {
//        return "Ack - vi har kontakt"
//    }
}