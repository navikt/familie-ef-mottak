package no.nav.familie.ef.mottak.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["spring.cloud.vault.enabled"])
class FlywayConfig {
    private val logger = LoggerFactory.getLogger(FlywayConfig::class.java)

    @Bean
    fun setRole(
        @Value("\${spring.cloud.vault.database.role}") role: String,
    ): FlywayConfigurationCustomizer {
        logger.info("Setter rolle $role")
        return FlywayConfigurationCustomizer { c -> c.initSql(String.format("SET ROLE \"%s\"", role)) }
    }
}
