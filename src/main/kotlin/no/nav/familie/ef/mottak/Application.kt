package no.nav.familie.ef.mottak

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import springfox.documentation.swagger2.annotations.EnableSwagger2

@SpringBootApplication(scanBasePackages = ["no.nav.familie"])
@EnableSwagger2
@ConfigurationPropertiesScan
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
