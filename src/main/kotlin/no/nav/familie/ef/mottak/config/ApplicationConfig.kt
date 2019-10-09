package no.nav.familie.ef.mottak.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.familie.ef.mottak.api.filter.RequestTimeFilter
import no.nav.familie.log.filter.LogFilter
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.data.jdbc.repository.config.JdbcConfiguration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.client.RestOperations
import javax.sql.DataSource

@SpringBootConfiguration
@EnableScheduling
class ApplicationConfig : JdbcConfiguration() {

    private val LOG = LoggerFactory.getLogger(ApplicationConfig::class.java)

    @Bean
    fun restTemplate(vararg interceptors: ClientHttpRequestInterceptor): RestOperations =
            RestTemplateBuilder().interceptors(*interceptors).build()

    @Bean
    fun dataSource(): DataSource {
        return EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.HSQL)
                .build()
    }

    @Bean
    fun operations(): NamedParameterJdbcOperations = NamedParameterJdbcTemplate(dataSource())

    @Bean
    fun transactionManager(): PlatformTransactionManager = DataSourceTransactionManager(dataSource())

    @Bean
    fun kotlinModule(): KotlinModule = KotlinModule()

    @Bean
    fun logFilter(): FilterRegistrationBean<LogFilter> {
        LOG.info("Registering LogFilter filter")
        val filterRegistration = FilterRegistrationBean<LogFilter>()
        filterRegistration.filter = LogFilter()
        filterRegistration.order = 1
        return filterRegistration
    }

    @Bean
    fun requestTimeFilter(): FilterRegistrationBean<RequestTimeFilter> {
        LOG.info("Registering RequestTimeFilter filter")
        val filterRegistration = FilterRegistrationBean<RequestTimeFilter>()
        filterRegistration.filter = RequestTimeFilter()
        filterRegistration.order = 2
        return filterRegistration
    }

}
