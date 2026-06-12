package no.nav.familie.ef.mottak

import no.nav.familie.ef.mottak.util.MockOAuth2ServerInitializer
import org.springframework.boot.builder.SpringApplicationBuilder

class ApplicationLocal : ApplicationLocalConfig()

fun main(args: Array<String>) {
    SpringApplicationBuilder(ApplicationLocal::class.java)
        .initializers(MockOAuth2ServerInitializer())
        .profiles(
            "local",
            "mock-integrasjon",
            "mock-dokument",
            "mock-ef-sak",
            "mock-pdf",
            "mock-pdf-soknad",
        ).run(*args)
}
