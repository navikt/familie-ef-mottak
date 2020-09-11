package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.config

import no.nav.familie.http.config.INaisProxyCustomizer
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
@Primary
class NaisNoProxyCustomizer : INaisProxyCustomizer {

    override fun customize(restTemplate: RestTemplate?) {
    }
}