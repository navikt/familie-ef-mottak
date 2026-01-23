package no.nav.familie.ef.mottak.config

import no.nav.familie.ef.mottak.encryption.CryptoService
import no.nav.familie.ef.mottak.encryption.FileCryptoReadingConverter
import no.nav.familie.ef.mottak.encryption.FileCryptoWritingConverter
import no.nav.familie.ef.mottak.encryption.StringValCryptoReadingConverter
import no.nav.familie.ef.mottak.encryption.StringValCryptoWritingConverter
import no.nav.familie.prosessering.PropertiesWrapperTilStringConverter
import no.nav.familie.prosessering.StringTilPropertiesWrapperConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.data.jdbc.core.JdbcAggregateOperations
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.data.jdbc.core.convert.DataAccessStrategy
import org.springframework.data.jdbc.core.convert.JdbcConverter
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource
import kotlin.collections.contains

@Configuration
@EnableJdbcAuditing
@EnableJdbcRepositories("no.nav.familie")
class DatabaseConfiguration : AbstractJdbcConfiguration() {
    @Bean
    fun namedParameterJdbcOperations(dataSource: DataSource): NamedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)

    @Bean
    fun transactionManager(dataSource: DataSource): PlatformTransactionManager = DataSourceTransactionManager(dataSource)

    @Bean
    override fun jdbcCustomConversions(): JdbcCustomConversions =
        JdbcCustomConversions(
            listOf(
                StringTilPropertiesWrapperConverter(),
                PropertiesWrapperTilStringConverter(),
                FileCryptoReadingConverter(),
                FileCryptoWritingConverter(),
                StringValCryptoReadingConverter(),
                StringValCryptoWritingConverter(),
            ),
        )

    @Bean
    fun verifyIgnoreIfProd(
        @Value("\${spring.flyway.placeholders.ignoreIfLocal}") ignoreIfLocal: String,
        environment: Environment,
    ): FlywayConfigurationCustomizer {
        val isLocal = environment.activeProfiles.contains("local") || environment.activeProfiles.contains("integrationtest")
        val ignore = ignoreIfLocal == "--"
        return FlywayConfigurationCustomizer {
            if (isLocal && !ignore) {
                throw RuntimeException("local eller integrationtest profile, men har ikke riktig verdi for placeholder ignoreIfLocal=$ignoreIfLocal")
            }
            if (!isLocal && ignore) {
                throw RuntimeException("Profile=${environment.activeProfiles} men har ignoreIfLocal=--")
            }
        }
    }
}
