package no.nav.familie.ef.mottak.config

import io.getunleash.strategy.Strategy
import no.nav.familie.ef.mottak.featuretoggle.ByEnvironmentStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CustomUnleashStrategies {
    @Bean
    fun strategies(): List<Strategy> {
        return listOf(ByEnvironmentStrategy())
    }
}
