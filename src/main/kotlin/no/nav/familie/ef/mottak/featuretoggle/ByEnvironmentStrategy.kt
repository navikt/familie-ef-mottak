package no.nav.familie.ef.mottak.featuretoggle

import io.getunleash.UnleashContext
import io.getunleash.strategy.Strategy

class ByEnvironmentStrategy : Strategy {
    companion object {
        private const val MILJØ_KEY = "miljø"
    }

    override fun getName(): String {
        return "byEnvironment"
    }

    override fun isEnabled(map: Map<String, String>): Boolean {
        return isEnabled(map, UnleashContext.builder().build())
    }

    override fun isEnabled(
        map: Map<String, String>,
        unleashContext: UnleashContext,
    ): Boolean {
        return unleashContext.environment
            .map { env -> map[MILJØ_KEY]?.split(',')?.contains(env) ?: false }
            .orElse(false)
    }
}
