package no.nav.familie.ef.mottak

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import springfox.documentation.swagger2.annotations.EnableSwagger2

@SpringBootApplication(scanBasePackages = ["no.nav.familie.ef.mottak"], exclude = [ErrorMvcAutoConfiguration::class])
@EnableSwagger2
class ApplicationLocal
fun main(args: Array<String>) {
    SpringApplicationBuilder(ApplicationLocal::class.java)
            .profiles("local")
            .run(*args)
}
