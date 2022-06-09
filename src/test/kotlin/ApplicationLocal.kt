package no.nav.familie.ef.mottak

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
@ConfigurationPropertiesScan
class ApplicationLocal

fun main(args: Array<String>) {
    SpringApplicationBuilder(ApplicationLocal::class.java)
        .profiles(
            "local",
            "mock-integrasjon",
            "mock-dokument",
            "mock-ef-sak",
            "mock-pdf"
        )
        .run(*args)
}
