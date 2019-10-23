package no.nav.familie.ef.mottak.api

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/api/"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PingController {

    @GetMapping("/ping")
    fun ping(): String {
        return "Ack - vi har kontakt"
    }

}