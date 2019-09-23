package no.nav.familie.ef.mottak.api.selftest;

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val APPLICATION_LIVENESS = "Application is alive!"

@RestController
@RequestMapping(value = ["/internal/status"])
class SelftestController(private val applicationState: ApplicationState) {

    val isAlive: ResponseEntity<String>
        @GetMapping(value = ["/isAlive"], produces = [MediaType.TEXT_PLAIN_VALUE])
        get() = if (applicationState.isAlive()) {
            ResponseEntity.ok(APPLICATION_LIVENESS)
        } else {
            ResponseEntity("Noe er galt", HttpStatus.INTERNAL_SERVER_ERROR)
        }
}
