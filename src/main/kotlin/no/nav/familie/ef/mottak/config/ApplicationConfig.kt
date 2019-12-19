package no.nav.familie.ef.mottak.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.familie.ef.mottak.api.filter.RequestTimeFilter
import no.nav.familie.ef.mottak.config.interceptor.BearerTokenClientInterceptor
import no.nav.familie.http.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.log.filter.LogFilter
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.web.client.RestOperations

@SpringBootConfiguration
@ComponentScan("no.nav.familie.prosessering",
               "no.nav.familie.sikkerhet",
               "no.nav.familie.ef.mottak"
)
@EnableJpaRepositories("no.nav.familie")
@EntityScan(basePackages = ["no.nav.familie"])
@ConfigurationPropertiesScan
@EnableJwtTokenValidation
@EnableOAuth2Client(cacheEnabled = true)
class ApplicationConfig(@Value("\${application.name}")
                        val applicationName: String) {

    private val logger = LoggerFactory.getLogger(ApplicationConfig::class.java)

    @Bean
    fun mdcValuesPropagatingClientInterceptor() = MdcValuesPropagatingClientInterceptor()

    @Bean
    fun restTemplate(mdcInterceptor: MdcValuesPropagatingClientInterceptor,
                     bearerTokenClientInterceptor: BearerTokenClientInterceptor): RestOperations {
        return RestTemplateBuilder().interceptors(mdcInterceptor, bearerTokenClientInterceptor).build()
    }

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
