package no.nav.familie.ef.mottak.config

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.felles.tokenklient.entraid.EntraIDClient
import no.nav.familie.felles.tokenklient.tokenx.TokenXClient
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TokenKlientTestConfig {
    @Bean
    @Primary
    fun entraIDClientMock(): EntraIDClient {
        val mock = mockk<EntraIDClient>(relaxed = true)
        every { mock.hentMaskinTilMaskinToken(any()) } returns "mock-m2m-token"
        every { mock.hentOboToken(any(), any()) } returns "mock-obo-token"
        return mock
    }

    @Bean
    @Primary
    fun tokenXClientMock(): TokenXClient {
        val mock = mockk<TokenXClient>(relaxed = true)
        every { mock.hentToken(any(), any()) } returns "mock-tokenx-token"
        return mock
    }
}
