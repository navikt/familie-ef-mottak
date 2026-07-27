package no.nav.familie.ef.mottak.config

import no.nav.familie.felles.tokenklient.entraid.EntraIDClient
import no.nav.familie.felles.tokenklient.entraid.MaskinTilMaskinTokenInterceptor
import no.nav.familie.felles.tokenklient.tokenx.TokenXClient
import no.nav.familie.felles.tokenklient.tokenx.TokenXInterceptor
import no.nav.familie.kafka.KafkaErrorHandler
import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.familie.log.NavSystemtype
import no.nav.familie.log.filter.LogFilter
import no.nav.familie.log.filter.RequestTimeFilter
import no.nav.familie.log.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.log.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.prosessering.config.ProsesseringInfoProvider
import no.nav.familie.restklient.config.RestTemplateBuilderBean
import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.familie.sikkerhet.context.FamilieFellesSpringSecurityKonfigurasjon
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.time.temporal.ChronoUnit

@SpringBootConfiguration
@ComponentScan(
    "no.nav.familie.prosessering",
    "no.nav.familie.ef.mottak",
    "no.nav.familie.felles.tokenklient.entraid",
    "no.nav.familie.felles.tokenklient.tokenx",
)
@ConfigurationPropertiesScan
@EnableScheduling
@Import(
    RestTemplateBuilderBean::class,
    MdcValuesPropagatingClientInterceptor::class,
    ConsumerIdClientInterceptor::class,
    KafkaErrorHandler::class,
    FamilieFellesSpringSecurityKonfigurasjon::class,
)
class ApplicationConfig {
    private val logger = LoggerFactory.getLogger(ApplicationConfig::class.java)

    @Bean("tokenExchange")
    fun restTemplateTokenExchange(
        tokenXClient: TokenXClient,
        @Value("\${familie.dokument.audience}") scope: String,
        mdcValuesPropagatingClientInterceptor: MdcValuesPropagatingClientInterceptor,
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
    ): RestOperations =
        RestTemplateBuilder()
            .connectTimeout(Duration.of(5, ChronoUnit.SECONDS))
            .readTimeout(Duration.of(25, ChronoUnit.SECONDS))
            .additionalMessageConverters(listOf(JacksonJsonHttpMessageConverter(jsonMapper)) + RestTemplate().messageConverters)
            .interceptors(
                TokenXInterceptor(tokenXClient, scope) { EksternBrukerUtils.getBearerTokenForLoggedInUser() },
                mdcValuesPropagatingClientInterceptor,
                consumerIdClientInterceptor,
            ).build()

    @Bean("restTemplateIntegrasjoner")
    fun restTemplateIntegrasjoner(
        entraIDClient: EntraIDClient,
        @Value("\${familie.ef.integrasjoner.scope}") scope: String,
        mdcInterceptor: MdcValuesPropagatingClientInterceptor,
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
    ): RestOperations = lagMaskinTilMaskinRestTemplate(entraIDClient, scope, mdcInterceptor, consumerIdClientInterceptor)

    @Bean("restTemplateSaksbehandling")
    fun restTemplateSaksbehandling(
        entraIDClient: EntraIDClient,
        @Value("\${familie.ef.saksbehandling.scope}") scope: String,
        mdcInterceptor: MdcValuesPropagatingClientInterceptor,
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
    ): RestOperations = lagMaskinTilMaskinRestTemplate(entraIDClient, scope, mdcInterceptor, consumerIdClientInterceptor)

    @Bean("restTemplatePdf")
    fun restTemplatePdf(
        entraIDClient: EntraIDClient,
        @Value("\${familie.pdf.scope}") scope: String,
        mdcInterceptor: MdcValuesPropagatingClientInterceptor,
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
    ): RestOperations = lagMaskinTilMaskinRestTemplate(entraIDClient, scope, mdcInterceptor, consumerIdClientInterceptor)

    private fun lagMaskinTilMaskinRestTemplate(
        entraIDClient: EntraIDClient,
        scope: String,
        mdcInterceptor: MdcValuesPropagatingClientInterceptor,
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
    ): RestOperations =
        RestTemplateBuilder()
            .connectTimeout(Duration.of(2, ChronoUnit.SECONDS))
            .readTimeout(Duration.of(5, ChronoUnit.MINUTES))
            .additionalMessageConverters(listOf(JacksonJsonHttpMessageConverter(jsonMapper)) + RestTemplate().messageConverters)
            .interceptors(
                mdcInterceptor,
                MaskinTilMaskinTokenInterceptor(entraIDClient, scope),
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
            .additionalMessageConverters(listOf(JacksonJsonHttpMessageConverter(jsonMapper)) + RestTemplate().messageConverters)
            .interceptors(mdcInterceptor, consumerIdClientInterceptor)
            .build()

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
        override fun hentBrukernavn(): String {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication is JwtAuthenticationToken) {
                return authentication.token.getClaimAsString("preferred_username")
                    ?: authentication.token.subject
                    ?: error("Finner ikke brukernavn i JWT")
            }
            error("Finner ikke brukernavn i security context")
        }

        override fun harTilgang(): Boolean {
            val authentication = SecurityContextHolder.getContext().authentication as? JwtAuthenticationToken
            val grupper = authentication?.token?.getClaimAsStringList("groups")?.toSet() ?: emptySet()
            return grupper.contains(prosesseringRolle)
        }
    }
}
