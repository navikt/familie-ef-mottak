package no.nav.familie.ef.mottak.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.familie.ef.mottak.api.filter.RequestTimeFilter
import no.nav.familie.log.filter.LogFilter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootConfiguration
@EnableJdbcRepositories
@EnableScheduling
class ApplicationConfig : AbstractJdbcConfiguration() {

    private val logger = LoggerFactory.getLogger(ApplicationConfig::class.java)

    @Bean
    @ConditionalOnProperty(name = ["spring.flyway.enabled"], havingValue = "false")
    fun dataSource(@Value("\${spring.datasource.username}") username: String,
                   @Value("\${spring.datasource.password}") password: String,
                   @Value("\${spring.datasource.url}") url: String): HikariDataSource {
        val hikariConf = HikariConfig()
        hikariConf.username = username
        hikariConf.password = password
        hikariConf.jdbcUrl = url
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
