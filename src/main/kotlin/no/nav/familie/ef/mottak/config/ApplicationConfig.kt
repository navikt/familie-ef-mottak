package no.nav.familie.ef.mottak.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.familie.http.client.RetryOAuth2HttpClient
import no.nav.familie.http.config.RestTemplateBuilderBean
import no.nav.familie.http.interceptor.BearerTokenClientInterceptor
import no.nav.familie.http.interceptor.BearerTokenExchangeClientInterceptor
import no.nav.familie.http.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.http.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.kafka.KafkaErrorHandler
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.log.NavSystemtype
import no.nav.familie.log.filter.LogFilter
import no.nav.familie.log.filter.RequestTimeFilter
import no.nav.familie.prosessering.config.ProsesseringInfoProvider
import no.nav.security.token.support.client.core.http.OAuth2HttpClient
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.time.temporal.ChronoUnit

@SpringBootConfiguration
@ComponentScan(
    "no.nav.familie.prosessering",
    "no.nav.familie.sikkerhet",
    "no.nav.familie.ef.mottak",
    "no.nav.familie.unleash",
)
@ConfigurationPropertiesScan
@EnableOAuth2Client(cacheEnabled = true)
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
@EnableScheduling
@Import(
    BearerTokenClientInterceptor::class,
    BearerTokenExchangeClientInterceptor::class,
    RestTemplateBuilderBean::class,
    MdcValuesPropagatingClientInterceptor::class,
    ConsumerIdClientInterceptor::class,
    KafkaErrorHandler::class,
)
class ApplicationConfig {
    private val logger = LoggerFactory.getLogger(ApplicationConfig::class.java)

    @Bean("tokenExchange")
    fun restTemplateTokenExchange(
        bearerTokenExchangeClientInterceptor: BearerTokenExchangeClientInterceptor,
        mdcValuesPropagatingClientInterceptor: MdcValuesPropagatingClientInterceptor,
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
    ): RestOperations =
        RestTemplateBuilder()
            .connectTimeout(Duration.of(5, ChronoUnit.SECONDS))
            .readTimeout(Duration.of(25, ChronoUnit.SECONDS))
            .additionalMessageConverters(listOf(MappingJackson2HttpMessageConverter(objectMapper)) + RestTemplate().messageConverters)
            .interceptors(
                bearerTokenExchangeClientInterceptor,
                mdcValuesPropagatingClientInterceptor,
                consumerIdClientInterceptor,
            ).build()

    @Bean("restTemplateAzure")
    fun restTemplateAzure(
        mdcInterceptor: MdcValuesPropagatingClientInterceptor,
        bearerTokenClientInterceptor: BearerTokenClientInterceptor,
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
    ): RestOperations =
        RestTemplateBuilder()
            .connectTimeout(Duration.of(2, ChronoUnit.SECONDS))
            .readTimeout(Duration.of(5, ChronoUnit.MINUTES))
            .additionalMessageConverters(listOf(MappingJackson2HttpMessageConverter(objectMapper)) + RestTemplate().messageConverters)
            .interceptors(
                mdcInterceptor,
                bearerTokenClientInterceptor,
                consumerIdClientInterceptor,
            ).build()

    @Bean("restTemplateUnsecured")
    fun restTemplateUnsecured(
        mdcInterceptor: MdcValuesPropagatingClientInterceptor,
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
    ): RestOperations =
        RestTemplateBuilder()
            .connectTimeout(Duration.of(2, ChronoUnit.SECONDS))
            .readTimeout(Duration.of(4, ChronoUnit.SECONDS))
            .additionalMessageConverters(listOf(MappingJackson2HttpMessageConverter(objectMapper)) + RestTemplate().messageConverters)
            .interceptors(mdcInterceptor, consumerIdClientInterceptor)
            .build()

    @Primary
    @Bean
    fun oAuth2HttpClient(): OAuth2HttpClient =
        RetryOAuth2HttpClient(
            RestClient.create(
                RestTemplateBuilder()
                    .connectTimeout(Duration.of(2, ChronoUnit.SECONDS))
                    .readTimeout(Duration.of(4, ChronoUnit.SECONDS))
                    .additionalMessageConverters(listOf(MappingJackson2HttpMessageConverter(objectMapper)) + RestTemplate().messageConverters)
                    .build(),
            ),
        )

    @Bean
    fun kotlinModule(): KotlinModule = KotlinModule.Builder().build()

    @Bean
    fun logFilter(): FilterRegistrationBean<LogFilter> =
        FilterRegistrationBean(LogFilter(systemtype = NavSystemtype.NAV_INTEGRASJON)).apply {
            logger.info("Registering LogFilter filter")
            order = 1
        }

    @Bean
    fun requestTimeFilter(): FilterRegistrationBean<RequestTimeFilter> =
        FilterRegistrationBean(RequestTimeFilter()).apply {
            logger.info("Registering RequestTimeFilter filter")
            order = 2
        }

    @Bean
    fun prosesseringInfoProvider(
        @Value("\${prosessering.rolle}") prosesseringRolle: String,
    ) = object :
        ProsesseringInfoProvider {
        override fun hentBrukernavn(): String =
            try {
                SpringTokenValidationContextHolder()
                    .getTokenValidationContext()
                    .getClaims("azuread")
                    .getStringClaim("preferred_username")
            } catch (e: Exception) {
                throw e
            }

        override fun harTilgang(): Boolean = grupper().contains(prosesseringRolle)

        private fun grupper(): List<String> =
            try {
                SpringTokenValidationContextHolder()
                    .getTokenValidationContext()
                    .getClaims(
                        "azuread",
                    ).get("groups") as List<String>? ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
    }
}
