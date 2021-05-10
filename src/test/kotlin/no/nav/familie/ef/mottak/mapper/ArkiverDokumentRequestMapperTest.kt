package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.config.*
import no.nav.familie.ef.mottak.mapper.ArkiverDokumentRequestMapper.toDto
import no.nav.familie.ef.mottak.repository.domain.Fil
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import no.nav.familie.ef.mottak.service.Testdata
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import java.util.*

internal class ArkiverDokumentRequestMapperTest {

    @Test
    internal fun `overgangsstønad toDto`() {
        val vedlegg = lagVedlegg()
        val dto = toDto(lagSøknad(Testdata.søknadOvergangsstønad, DOKUMENTTYPE_OVERGANGSSTØNAD), listOf(vedlegg))
        assertThat(dto.vedleggsdokumenter.first().dokumenttype)
                .isEqualTo(Dokumenttype.OVERGANGSSTØNAD_SØKNAD_VEDLEGG)
        assertThat(dto.vedleggsdokumenter.first().filnavn).isEqualTo(vedlegg.id.toString())
    }

    @Test
    internal fun `barnetilsyn toDto`() {
        val dto = toDto(lagSøknad(Testdata.søknadBarnetilsyn, DOKUMENTTYPE_BARNETILSYN), listOf(lagVedlegg()))
        assertThat(dto.vedleggsdokumenter.first().dokumenttype)
                .isEqualTo(Dokumenttype.BARNETILSYNSTØNAD_VEDLEGG)
    }

    @Test
    internal fun `skolepenger toDto`() {
        val dto = toDto(lagSøknad(Testdata.søknadSkolepenger, DOKUMENTTYPE_SKOLEPENGER), listOf(lagVedlegg()))
        assertThat(dto.vedleggsdokumenter.first().dokumenttype)
                .isEqualTo(Dokumenttype.SKOLEPENGER_VEDLEGG)
    }

    @Test
    internal fun `arbeidssøker toDto`() {
        toDto(lagSøknad(Testdata.skjemaForArbeidssøker, DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER), emptyList())
    }

    @Test
    internal fun `arbeidssøker toDto med vedlegg skal feile`() {
        assertThat(catchThrowable {
            toDto(lagSøknad(Testdata.skjemaForArbeidssøker, DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER), listOf(lagVedlegg()))
        }).withFailMessage("Skjema arbeidssøker kan ikke ha vedlegg")
    }

    private fun lagSøknad(søknad: Any, dokumenttype: String): Søknad = Søknad(
            søknadJson = objectMapper.writeValueAsString(søknad),
            fnr = "123",
            søknadPdf = Fil(byteArrayOf(12)),
            dokumenttype = dokumenttype
    )

    private fun lagVedlegg() = Vedlegg(UUID.randomUUID(), "id", "navn", "tittel", Fil(byteArrayOf(12)))
}