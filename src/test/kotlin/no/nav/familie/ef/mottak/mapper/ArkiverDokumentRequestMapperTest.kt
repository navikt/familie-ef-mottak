package no.nav.familie.ef.mottak.mapper

import com.fasterxml.jackson.module.kotlin.readValue
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
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Filtype
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import java.util.UUID

internal class ArkiverDokumentRequestMapperTest {
    @Test
    internal fun `overgangsstønad toDto - sjekk alle felt`() {
        val vedlegg = lagVedlegg()
        val søknad = lagSøknad(Testdata.søknadOvergangsstønad, DOKUMENTTYPE_OVERGANGSSTØNAD)
        val dto = toDto(søknad, listOf(vedlegg))
        assertThat(dto.fnr).isEqualTo(søknad.fnr)
        assertThat(dto.forsøkFerdigstill).isFalse
        assertThat(dto.hoveddokumentvarianter).hasSize(2)
        assertThat(
            dto.hoveddokumentvarianter
                .first()
                .dokumenttype.name,
        ).isEqualTo(søknad.dokumenttype)
        assertThat(dto.hoveddokumentvarianter.first().tittel).isEqualTo(Dokumenttype.valueOf(søknad.dokumenttype).dokumentTittel())
        assertThat(dto.hoveddokumentvarianter.first().filtype).isEqualTo(Filtype.PDFA)
        assertThat(dto.vedleggsdokumenter.first().dokumenttype)
            .isEqualTo(Dokumenttype.OVERGANGSSTØNAD_SØKNAD_VEDLEGG)
        assertThat(dto.vedleggsdokumenter.first().filnavn).isEqualTo(vedlegg.id.toString())
    }

    @Test
    internal fun `Skal bruke kryptert søknadsdata hvis json er null`() {
        val søknadsdata = "{ \"test\": \"data\" }"
        val søknad = lagSøknad(søknadsdata, DOKUMENTTYPE_OVERGANGSSTØNAD, json = null)
        assertThat(søknad.json).isNull()
        val dto = toDto(søknad, emptyList())
        val string = objectMapper.readValue<String>(dto.hoveddokumentvarianter.last().dokument)
        assertThat(string).isEqualTo(søknadsdata)
    }

    @Test
    internal fun `Skal bruke ukryptert json hvis json finnes`() {
        val json = "{ \"test\": \"data\" }"
        val søknad = lagSøknad(Testdata.søknadOvergangsstønad, DOKUMENTTYPE_OVERGANGSSTØNAD, json = json)
        val dto = toDto(søknad, emptyList())
        val string = objectMapper.readValue<String>(dto.hoveddokumentvarianter.last().dokument)
        assertThat(string).isEqualTo(json)
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
            },
        ).withFailMessage("Skjema arbeidssøker kan ikke ha vedlegg")
    }

    private fun lagSøknad(
        søknad: Any,
        dokumenttype: String,
        json: Any? =null ,
    ): Søknad {
        val søknadsdata = objectMapper.writeValueAsString(søknad)
        val jsonval : String? = json?.let { objectMapper.writeValueAsString(json)}
        return Søknad(
            søknadJson = EncryptedString(søknadsdata),
            fnr = "123",
            søknadPdf = EncryptedFile(byteArrayOf(12)),
            dokumenttype = dokumenttype,
            json = jsonval,
        )
    }

    private fun lagVedlegg() = Vedlegg(UUID.randomUUID(), "id", "navn", "tittel", EncryptedFile(byteArrayOf(12)))
}
