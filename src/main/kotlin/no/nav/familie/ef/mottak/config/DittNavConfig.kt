package no.nav.familie.ef.mottak.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("dittnav")
@ConstructorBinding
data class DittNavConfig(val soknadfrontendUrl: String)
