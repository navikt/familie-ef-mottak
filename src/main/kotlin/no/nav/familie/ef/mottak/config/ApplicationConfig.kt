package no.nav.familie.ef.mottak.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.familie.http.client.RetryOAuth2HttpClient
import no.nav.familie.http.config.RestTemplateBuilderBean
import no.nav.familie.http.interceptor.BearerTokenClientInterceptor
import no.nav.familie.http.interceptor.BearerTokenExchangeClientInterceptor
import no.nav.familie.http.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.http.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.log.filter.LogFilter
import no.nav.familie.log.filter.RequestTimeFilter
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.client.RestOperations
import java.time.Duration
import java.time.temporal.ChronoUnit

@SpringBootConfiguration
@ComponentScan("no.nav.familie.prosessering",
               "no.nav.familie.sikkerhet",
               "no.nav.familie.ef.mottak")
@EnableJpaRepositories("no.nav.familie")
@EntityScan(basePackages = ["no.nav.familie"])
@ConfigurationPropertiesScan
@EnableOAuth2Client(cacheEnabled = true)
@EnableJwtTokenValidation(ignore = ["org.springframework", "springfox.documentation.swagger"])
@EnableScheduling
@Import(BearerTokenClientInterceptor::class,
        BearerTokenExchangeClientInterceptor::class,
        RestTemplateBuilderBean::class,
        MdcValuesPropagatingClientInterceptor::class,
        ConsumerIdClientInterceptor::class)
class ApplicationConfig {

    private val logger = LoggerFactory.getLogger(ApplicationConfig::class.java)


    @Bean("tokenExchange")
    fun restTemplate(bearerTokenExchangeClientInterceptor: BearerTokenExchangeClientInterceptor,
                     mdcValuesPropagatingClientInterceptor: MdcValuesPropagatingClientInterceptor,
                     consumerIdClientInterceptor: ConsumerIdClientInterceptor): RestOperations {
        return RestTemplateBuilder()
                .setConnectTimeout(Duration.of(5, ChronoUnit.SECONDS))
                .setReadTimeout(Duration.of(25, ChronoUnit.SECONDS))
                .interceptors(bearerTokenExchangeClientInterceptor,
                              mdcValuesPropagatingClientInterceptor,
                              consumerIdClientInterceptor)
                .build()
    }

    @Bean("restTemplateAzure")
    fun restTemplateAzure(mdcInterceptor: MdcValuesPropagatingClientInterceptor,
                          bearerTokenClientInterceptor: BearerTokenClientInterceptor,
                          consumerIdClientInterceptor: ConsumerIdClientInterceptor): RestOperations {
        return RestTemplateBuilder()
                .setConnectTimeout(Duration.of(2, ChronoUnit.SECONDS))
                .setReadTimeout(Duration.of(5, ChronoUnit.MINUTES))
                .interceptors(mdcInterceptor,
                              bearerTokenClientInterceptor,
                              consumerIdClientInterceptor)
                .build()
    }

    @Bean("restTemplateUnsecured")
    fun restTemplate(mdcInterceptor: MdcValuesPropagatingClientInterceptor,
                     consumerIdClientInterceptor: ConsumerIdClientInterceptor): RestOperations {
        return RestTemplateBuilder()
                .setConnectTimeout(Duration.of(2, ChronoUnit.SECONDS))
                .setReadTimeout(Duration.of(4, ChronoUnit.SECONDS))
                .interceptors(mdcInterceptor, consumerIdClientInterceptor).build()
    }

    @Primary
    @Bean
    fun oAuth2HttpClient(): RetryOAuth2HttpClient {
        return RetryOAuth2HttpClient(RestTemplateBuilder()
                                             .setConnectTimeout(Duration.of(2, ChronoUnit.SECONDS))
                                             .setReadTimeout(Duration.of(4, ChronoUnit.SECONDS)))
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
