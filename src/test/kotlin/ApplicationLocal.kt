package no.nav.familie.ef.mottak

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Import

@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
@ConfigurationPropertiesScan
@Import(TokenGeneratorConfiguration::class)
class ApplicationLocal

fun main(args: Array<String>) {
    SpringApplicationBuilder(ApplicationLocal::class.java)
            .profiles("local",
                      "mock-integrasjon",
                      "mock-ef-sak",
                      "mock-pdf")
            .run(*args)
}
