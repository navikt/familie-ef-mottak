package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/"], produces = [MediaType.APPLICATION_JSON_VALUE])
class TogglePingTestController (val featureToggleService: FeatureToggleService){

    private val logger = LoggerFactory.getLogger(this::class.java)

    // TODO denne skal ikke merges til master
    @GetMapping("/togglePingTest")
    @Unprotected
    fun pingToggle(): String {
        val toggle = featureToggleService.isEnabled("familie.ef.mottak.togglePing")
        logger.info("familie.ef.mottak.togglePing = $toggle")
        return "Ack - unleash svarer: $toggle"
    }
}
