package no.nav.familie.ef.mottak.featuretoggle

import no.nav.familie.unleash.UnleashService
import org.springframework.stereotype.Service

@Service
class UnleashNextService(
    private val unleashService: UnleashService,
) {
    fun isEnabled(toggle: Toggle): Boolean {

        return unleashService.isEnabled(
            toggleId = toggle.toggleId,
        )
    }
}
