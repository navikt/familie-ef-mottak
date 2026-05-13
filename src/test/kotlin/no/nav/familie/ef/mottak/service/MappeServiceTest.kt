package no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeResponseDto
import no.nav.familie.kontrakter.felles.oppgave.MappeDto
import org.assertj.core.api.Assertions
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
                dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                journalpostId = "1234",
                fnr = Testdata.randomFnr(),
                behandleINySaksbehandling = true,
                json = jsonMapper.writeValueAsString(Testdata.søknadOvergangsstønad),
            )
        every { integrasjonerClient.finnMappe(any()) } returns
            FinnMappeResponseDto(
                antallTreffTotalt = 1,
                mapper =
                    listOf(
                        MappeDto(id = 123, navn = "65 Opplæring", enhetsnr = ""),
                        MappeDto(id = 234, navn = "60 Særlig tilsynskrevende", enhetsnr = ""),
                        MappeDto(id = 345, navn = "61 Selvstendig næringsdrivende", enhetsnr = ""),
                    ),
            )
    }

    @Test
    internal fun `skal bruke cache når man henter mapper for samme enhet flere ganger`() {
        (1..20).forEach { _ -> mappeService.finnMappeIdForSøknadOgEnhet(søknadId, enhet) }

        verify(exactly = 1) { integrasjonerClient.finnMappe(any()) }
    }

    @Test
    fun `regelendring 2026 - barnSærligTilsyn gir mappe særlig tilsynskrevende`() {
        val søknad =
            lagRegelendring2026Søknad(
                hvaSituasjonSvarId = listOf("barnUnder14Måneder", "barnSærligTilsyn"),
                inntekterSvarId = listOf("arbeidstaker"),
            )
        every { søknadService.hentSøknad(søknadId) } returns søknad

        val mappeId = mappeService.finnMappeIdForSøknadOgEnhet(søknadId, enhet)

        Assertions.assertThat(mappeId).isEqualTo(234L)
    }

    @Test
    fun `regelendring 2026 - selvstendigNæringsdrivende gir mappe selvstendig`() {
        val søknad =
            lagRegelendring2026Søknad(
                hvaSituasjonSvarId = listOf("barnUnder14Måneder"),
                inntekterSvarId = listOf("selvstendigNæringsdrivende"),
            )
        every { søknadService.hentSøknad(søknadId) } returns søknad

        val mappeId = mappeService.finnMappeIdForSøknadOgEnhet(søknadId, enhet)

        Assertions.assertThat(mappeId).isEqualTo(345L)
    }

    private fun lagRegelendring2026Søknad(
        hvaSituasjonSvarId: List<String>,
        inntekterSvarId: List<String>,
    ): Søknad {
        val søknadData =
            Testdata.søknadOvergangsstønadRegelendring2026.copy(
                hvaSituasjon =
                    Testdata.søknadOvergangsstønadRegelendring2026.hvaSituasjon.copy(
                        svarId = hvaSituasjonSvarId,
                    ),
                inntekter =
                    Testdata.søknadOvergangsstønadRegelendring2026.inntekter.copy(
                        svarId = inntekterSvarId,
                    ),
            )
        return Søknad(
            dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
            journalpostId = "1234",
            fnr = Testdata.randomFnr(),
            behandleINySaksbehandling = true,
            json = jsonMapper.writeValueAsString(søknadData),
        )
    }
}
