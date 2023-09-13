package no.nav.familie.ef.mottak.featuretoggle

import org.springframework.stereotype.Service

@Service
class FeatureToggleService {

    fun isEnabled(toggleId: String): Boolean {
        return isEnabled(toggleId, false)
    }

    fun isEnabled(toggleId: String, defaultValue: Boolean): Boolean {
        return isEnabled(toggleId, defaultValue)
    }
}
