package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/"], produces = [MediaType.APPLICATION_JSON_VALUE])
class TogglePingTestController (val featureToggleService: FeatureToggleService){

    @GetMapping("/ping")
    @Unprotected
    fun pingToggle(): String {
        featureToggleService.isEnabled("it")
        return "Ack - vi har kontakt fea"
    }
}
