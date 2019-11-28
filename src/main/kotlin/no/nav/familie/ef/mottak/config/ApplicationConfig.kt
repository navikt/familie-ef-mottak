package no.nav.familie.ef.mottak.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.familie.ef.mottak.api.filter.RequestTimeFilter
import no.nav.familie.http.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.log.filter.LogFilter
import no.nav.familie.sikkerhet.OIDCUtil
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
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
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.client.RestOperations

@SpringBootConfiguration
@ComponentScan("no.nav.familie.prosessering"
               ,
               "no.nav.security.token.support.client.spring.oauth2"
)
@EnableJpaRepositories("no.nav.familie")
@EntityScan(basePackages = ["no.nav.familie"])
//@ConfigurationPropertiesScan
@EnableScheduling
class ApplicationConfig(@Value("\${application.name}") val applicationName: String) {

    private val logger = LoggerFactory.getLogger(ApplicationConfig::class.java)

    @Bean
    fun mdcValuesPropagatingClientInterceptor() = MdcValuesPropagatingClientInterceptor(applicationName)

    @Bean
    fun restTemplate(vararg interceptors: ClientHttpRequestInterceptor): RestOperations =
            RestTemplateBuilder().interceptors(*interceptors).build()

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

//    @Bean
//    fun ctHolder() = SpringTokenValidationContextHolder()


    @Bean
    fun oidcUtil(ctxHolder: TokenValidationContextHolder): OIDCUtil = OIDCUtil(ctxHolder)

}
