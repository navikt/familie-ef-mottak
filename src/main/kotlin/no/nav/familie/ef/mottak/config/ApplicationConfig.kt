package no.nav.familie.ef.mottak.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.familie.ef.mottak.api.filter.RequestTimeFilter
import no.nav.familie.log.filter.LogFilter
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.PlatformTransactionManager

@SpringBootConfiguration
@EnableJdbcRepositories
@EnableScheduling
class ApplicationConfig : AbstractJdbcConfiguration() {

    private val logger = LoggerFactory.getLogger(ApplicationConfig::class.java)

    @Bean
    fun dataSource(): HikariDataSource {
        val hikariConf = HikariConfig()
        hikariConf.username = "postgres"
        hikariConf.password = "test"
        hikariConf.jdbcUrl = "jdbc:postgresql://0.0.0.0:5432/familie-ef-mottak?stringtype=unspecified"
        val hikariDataSource = HikariDataSource(hikariConf)
        return hikariDataSource
    }

    /*
    @Bean
    fun operations(): NamedParameterJdbcOperations = NamedParameterJdbcTemplate(dataSource())

    @Bean
    fun transactionManager(): PlatformTransactionManager = DataSourceTransactionManager(dataSource())
     */
    @Bean
    fun kotlinModule(): KotlinModule = KotlinModule()

    @Bean
    fun logFilter(): FilterRegistrationBean<LogFilter> {
        logger.info("Registering LogFilter filter")
        val filterRegistration = FilterRegistrationBean<LogFilter>()
        filterRegistration.filter = LogFilter()
        filterRegistration.order = 1
        return filterRegistration
    }

    @Bean
    fun requestTimeFilter(): FilterRegistrationBean<RequestTimeFilter> {
        logger.info("Registering RequestTimeFilter filter")
        val filterRegistration = FilterRegistrationBean<RequestTimeFilter>()
        filterRegistration.filter = RequestTimeFilter()
        filterRegistration.order = 2
        return filterRegistration
    }

}
