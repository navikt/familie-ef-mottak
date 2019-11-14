package no.nav.familie.ef.mottak

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Import
import springfox.documentation.swagger2.annotations.EnableSwagger2

@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
@EnableSwagger2
@Import(TokenGeneratorConfiguration::class)
@EnableJwtTokenValidation(ignore = ["org.springframework", "springfox.documentation.swagger.web.ApiResourceController"])
@ConfigurationPropertiesScan
class ApplicationLocal
fun main(args: Array<String>) {
    SpringApplicationBuilder(ApplicationLocal::class.java)
            .profiles("local")
            .run(*args)
}
