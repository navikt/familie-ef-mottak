package no.nav.familie.ef.mottak.config

import no.nav.familie.ef.mottak.config.interceptor.BearerTokenClientInterceptor
import no.nav.familie.http.interceptor.MdcValuesPropagatingClientInterceptor
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestOperations

@Configuration
class RestTemplateTestConfiguration {

    @Bean("restTemplate")
    @Primary
    fun restTemplateLocal(mdcInterceptor: MdcValuesPropagatingClientInterceptor,
                          bearerTokenClientInterceptor: BearerTokenClientInterceptor): RestOperations {
        return RestTemplateBuilder()
                .interceptors(mdcInterceptor, bearerTokenClientInterceptor)
                .build()
    }

}

