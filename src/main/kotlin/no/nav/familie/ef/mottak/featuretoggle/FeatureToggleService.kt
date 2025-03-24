package no.nav.familie.ef.mottak.featuretoggle

import org.springframework.stereotype.Service

@Service
class FeatureToggleService(
    val unleashNextService: UnleashNextService,
) {
    fun isEnabled(toggle: Toggle): Boolean = unleashNextService.isEnabled(toggle)
}

enum class Toggle(
    val toggleId: String,
    val beskrivelse: String? = null,
) {
    ENDRE_TEST("Endre-test-toggle"),
    ;

    companion object {
        private val toggles: Map<String, Toggle> = values().associateBy { it.name }

        fun byToggleId(toggleId: String): Toggle = toggles[toggleId] ?: error("Finner ikke toggle for $toggleId")
    }
}
