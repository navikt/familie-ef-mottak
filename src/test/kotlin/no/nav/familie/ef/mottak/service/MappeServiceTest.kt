package no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeResponseDto
import no.nav.familie.kontrakter.felles.oppgave.MappeDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.cache.concurrent.ConcurrentMapCacheManager

internal class MappeServiceTest {
    private val integrasjonerClient: IntegrasjonerClient = mockk()
    private val søknadService: SøknadService = mockk()
    private val cacheManager = ConcurrentMapCacheManager()

    val mappeService = MappeService(integrasjonerClient, søknadService, cacheManager)

    val søknadId = "123"
    val enhet = "4489"

    @BeforeEach
    internal fun setUp() {
        every {
            søknadService.hentSøknad("123")
        } returns
            Søknad(
                søknadJson = EncryptedString(objectMapper.writeValueAsString(Testdata.søknadOvergangsstønad)),
                dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                journalpostId = "1234",
                fnr = Testdata.randomFnr(),
                behandleINySaksbehandling = true,
                json = "{}",
            )
        every { integrasjonerClient.finnMappe(any()) } returns
            FinnMappeResponseDto(
                antallTreffTotalt = 1,
                mapper =
                    listOf(
                        MappeDto(id = 123, navn = "65 Opplæring", enhetsnr = ""),
                        MappeDto(id = 234, navn = "60 Særlig tilsynskrevende", enhetsnr = ""),
                    ),
            )
    }

    @Test
    internal fun `skal bruke cache når man henter mapper for samme enhet flere ganger`() {
        (1..20).forEach { _ -> mappeService.finnMappeIdForSøknadOgEnhet(søknadId, enhet) }

        verify(exactly = 1) { integrasjonerClient.finnMappe(any()) }
    }
}
