package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.exchange

internal class ForvaltningAuthTest : IntegrasjonSpringRunnerTest() {
    @Test
    internal fun `forvaltning skal avvise TokenX-token med 401`() {
        val tokenxHeaders = HttpHeaders().apply { setBearerAuth(søkerBearerToken()) }
        val exception =
            assertThrows<HttpClientErrorException.Unauthorized> {
                restTemplate.exchange<Any>(
                    localhost("/api/forvaltning/ettersending/splitt"),
                    HttpMethod.POST,
                    HttpEntity("{}", tokenxHeaders),
                )
            }
        assertThat(exception.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    internal fun `statistikk skal avvise TokenX-token med 401`() {
        val tokenxHeaders = HttpHeaders().apply { setBearerAuth(søkerBearerToken()) }
        val exception =
            assertThrows<HttpClientErrorException.Unauthorized> {
                restTemplate.exchange<Any>(
                    localhost("/api/statistikk/soknader"),
                    HttpMethod.GET,
                    HttpEntity<Void>(tokenxHeaders),
                )
            }
        assertThat(exception.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    internal fun `statistikk skal akseptere Azure AD-token`() {
        val azureHeaders = HttpHeaders().apply { setBearerAuth(saksbehandlerBearerToken()) }
        try {
            restTemplate.exchange<Any>(
                localhost("/api/statistikk/soknader"),
                HttpMethod.GET,
                HttpEntity<Void>(azureHeaders),
            )
        } catch (e: HttpClientErrorException.Unauthorized) {
            throw AssertionError("Azure AD-token ble avvist med 401, men burde ha blitt akseptert", e)
        } catch (_: HttpServerErrorException) {
            // 500 fra business-logikk er OK - auth er godkjent
        }
    }
}
