package no.nav.familie.ef.mottak.featuretoggle

import no.nav.familie.unleash.UnleashService
import org.springframework.stereotype.Service

@Service
class FeatureToggleService(
    val unleashService: UnleashService,
) {
    fun isEnabled(toggleId: String): Boolean = unleashService.isEnabled(toggleId, false)
}
