package no.nav.familie.ef.mottak.config

import no.nav.familie.http.config.NaisProxyCustomizer
import no.nav.familie.http.interceptor.BearerTokenClientInterceptor
import no.nav.familie.http.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.http.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.kontrakter.felles.objectMapper
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.converter.ByteArrayHttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import java.nio.charset.StandardCharsets

@Configuration
@Import(
        ConsumerIdClientInterceptor::class,
        BearerTokenClientInterceptor::class,
        MdcValuesPropagatingClientInterceptor::class)
class RestTemplateConfig {


    @Bean("restTemplateAzure")
    fun restTemplateJwtBearer(consumerIdClientInterceptor: ConsumerIdClientInterceptor,
                              bearerTokenClientInterceptor: BearerTokenClientInterceptor): RestOperations {
        return RestTemplateBuilder()
                .additionalCustomizers(NaisProxyCustomizer())
                .interceptors(consumerIdClientInterceptor,
                              bearerTokenClientInterceptor,
                              MdcValuesPropagatingClientInterceptor())
                .additionalMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
                .build()
    }

    @Bean("restTemplateUnsecured")
    fun restTemplate(restTemplateBuilder: RestTemplateBuilder,
                     mdcInterceptor: MdcValuesPropagatingClientInterceptor,
                     consumerIdClientInterceptor: ConsumerIdClientInterceptor): RestOperations {
        return restTemplateBuilder.interceptors(mdcInterceptor, consumerIdClientInterceptor).build()
    }

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate(listOf(StringHttpMessageConverter(StandardCharsets.UTF_8),
                                   ByteArrayHttpMessageConverter(),
                                   MappingJackson2HttpMessageConverter(objectMapper)))
    }
}
