package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.mapper.ArkiverDokumentRequestMapper.toDto
import no.nav.familie.ef.mottak.repository.domain.EncryptedFile
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import no.nav.familie.ef.mottak.service.Testdata
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import java.util.UUID

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
        assertThat(
            catchThrowable {
                toDto(lagSøknad(Testdata.skjemaForArbeidssøker, DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER), listOf(lagVedlegg()))
            }
        ).withFailMessage("Skjema arbeidssøker kan ikke ha vedlegg")
    }

    private fun lagSøknad(søknad: Any, dokumenttype: String): Søknad = Søknad(
        søknadJson = EncryptedString(objectMapper.writeValueAsString(søknad)),
        fnr = "123",
        søknadPdf = EncryptedFile(byteArrayOf(12)),
        dokumenttype = dokumenttype
    )

    private fun lagVedlegg() = Vedlegg(UUID.randomUUID(), "id", "navn", "tittel", EncryptedFile(byteArrayOf(12)))
}
