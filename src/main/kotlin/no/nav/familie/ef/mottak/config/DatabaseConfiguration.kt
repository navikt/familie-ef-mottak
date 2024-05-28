package no.nav.familie.ef.mottak.config

import no.nav.familie.ef.mottak.encryption.FileCryptoReadingConverter
import no.nav.familie.ef.mottak.encryption.FileCryptoWritingConverter
import no.nav.familie.ef.mottak.encryption.StringValCryptoReadingConverter
import no.nav.familie.ef.mottak.encryption.StringValCryptoWritingConverter
import no.nav.familie.prosessering.PropertiesWrapperTilStringConverter
import no.nav.familie.prosessering.StringTilPropertiesWrapperConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
@EnableJdbcAuditing
@EnableJdbcRepositories("no.nav.familie")
class DatabaseConfiguration : AbstractJdbcConfiguration() {
    @Bean
    fun namedParameterJdbcOperations(dataSource: DataSource): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(dataSource)
    }

    @Bean
    fun transactionManager(dataSource: DataSource): PlatformTransactionManager {
        return DataSourceTransactionManager(dataSource)
    }

    @Bean
    override fun jdbcCustomConversions(): JdbcCustomConversions {
        return JdbcCustomConversions(
            listOf(
                StringTilPropertiesWrapperConverter(),
                PropertiesWrapperTilStringConverter(),
                FileCryptoReadingConverter(),
                FileCryptoWritingConverter(),
                StringValCryptoReadingConverter(),
                StringValCryptoWritingConverter(),
            ),
        )
    }
}
