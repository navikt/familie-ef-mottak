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

    @GetMapping("/status")
    @Unprotected
    fun status(): StatusDto {
        return StatusDto(status = Plattformstatus.OK, description = "Alt er bra", logLink = null)
    }

}

const val LOG_URL = "https://logs.adeo.no/app/discover#/view/a3e93b80-c1a5-11ee-a029-75a0ed43c092?_g=()"
data class StatusDto(val status: Plattformstatus, val description: String? = null, val logLink: String? = LOG_URL)

enum class Plattformstatus {
    OK, ISSUE, DOWN
}
