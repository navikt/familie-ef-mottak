package no.nav.familie.ef.mottak.featuretoggle

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

enum class Toggle(val toggleId: String) {
    TEST_ENVIRONMENT("test.environment");
}

/**
 * TODO : Fjern når toggles er verifisert ok ift Unleash Next
 */
@RestController
@RequestMapping(path = ["/api/featuretoggle"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Unprotected
class FeatureToggleController(val featureToggleService: FeatureToggleService) {
    private val funksjonsbrytere = setOf(
        Toggle.TEST_ENVIRONMENT
    )

    @GetMapping
    fun sjekkAlle(): Map<String, Boolean> {
        return funksjonsbrytere.associate { it.toggleId to featureToggleService.isEnabled(it.toggleId) }
    }
}
