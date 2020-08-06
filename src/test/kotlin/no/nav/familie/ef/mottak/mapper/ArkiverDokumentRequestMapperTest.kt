package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.config.*
import no.nav.familie.ef.mottak.mapper.ArkiverDokumentRequestMapper.toDto
import no.nav.familie.ef.mottak.repository.domain.Fil
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import no.nav.familie.ef.mottak.service.Testdata
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import java.util.*

internal class ArkiverDokumentRequestMapperTest {

    @Test
    internal fun `overgangsstønad toDto`() {
        val dto = toDto(lagSøknad(Testdata.søknadOvergangsstønad, DOKUMENTTYPE_OVERGANGSSTØNAD), listOf(lagVedlegg()))
        assertThat(dto.vedleggsdokumenter.first().dokumentType)
                .isEqualTo(DOKUMENTTYPE_OVERGANGSSTØNAD_VEDLEGG)
    }

    @Test
    internal fun `barnetilsyn toDto`() {
        val dto = toDto(lagSøknad(Testdata.søknadBarnetilsyn, DOKUMENTTYPE_BARNETILSYN), listOf(lagVedlegg()))
        assertThat(dto.vedleggsdokumenter.first().dokumentType)
                .isEqualTo(DOKUMENTTYPE_BARNETILSYN_VEDLEGG)
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

    private fun lagSøknad(søknad: Any, dokumenttype: String): Soknad = Soknad(
            søknadJson = objectMapper.writeValueAsString(søknad),
            fnr = "123",
            søknadPdf = Fil(byteArrayOf(12)),
            dokumenttype = dokumenttype
    )

    private fun lagVedlegg() = Vedlegg(UUID.randomUUID(), "id", "navn", "tittel", Fil(byteArrayOf(12)))
}