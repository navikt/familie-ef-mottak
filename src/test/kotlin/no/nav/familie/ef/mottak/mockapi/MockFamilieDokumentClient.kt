package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.mockapi

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.mottak.integration.FamilieDokumentClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

fun mockFamilieDokumentClient(): FamilieDokumentClient {
    val mockk = mockk<FamilieDokumentClient>()
    every { mockk.hentVedlegg(any()) } answers {
        "vedlegg".toByteArray()
    }
    return mockk
}

@Profile("local")
@Configuration
class FamilieDokumentConfiguration {

    @Bean
    @Primary
    fun familieDokumentClient() = mockFamilieDokumentClient()
}
