package no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.ef.mottak.repository.DokumentasjonsbehovRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.EncryptedFile
import no.nav.familie.kontrakter.ef.søknad.Dokumentasjonsbehov
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class SøknadskvitteringServiceTest {
    private val søknadRepository = mockk<SøknadRepository>(relaxed = true)
    private val vedleggRepository = mockk<VedleggRepository>(relaxed = true)
    private val dokumentasjonsbehovRepository = mockk<DokumentasjonsbehovRepository>(relaxed = true)
    private val taskProsesseringService = mockk<TaskProsesseringService>(relaxed = true)

    private val søknadskvitteringService =
        SøknadskvitteringService(søknadRepository, vedleggRepository, mockk(), dokumentasjonsbehovRepository, taskProsesseringService)

    @Test
    internal fun `hentDokumentasjonsbehovforPerson fungerer for overgangsstønad, barnetilsyn og skolepenger`() {
        val fnr = "12345678"
        val søknader =
            listOf(
                SøknadMapper.fromDto(Testdata.søknadOvergangsstønad, false),
                SøknadMapper.fromDto(Testdata.søknadBarnetilsyn, false),
                SøknadMapper.fromDto(Testdata.søknadSkolepenger, false),
                SøknadMapper.fromDto(Testdata.skjemaForArbeidssøker),
            )
        val forventetDokumentasjonsbehov =
            listOf(
                Dokumentasjonsbehov(
                    "test",
                    UUID
                        .randomUUID()
                        .toString(),
                    false,
                ),
            )

        every { søknadRepository.finnSisteSøknadenPerStønadtype(fnr) } returns søknader

        every { dokumentasjonsbehovRepository.findByIdOrNull(any()) }
            .returns(
                no.nav.familie.ef.mottak.repository.domain
                    .Dokumentasjonsbehov("123", objectMapper.writeValueAsString(forventetDokumentasjonsbehov)),
            )

        every { søknadRepository.findByIdOrNull(any()) } returns SøknadMapper.fromDto(Testdata.søknadOvergangsstønad, false)

        assertThat(søknadskvitteringService.hentDokumentasjonsbehovForPerson(fnr)).hasSize(3)
    }

    @Nested
    inner class ReduserSøknad {
        @Test
        fun `for søknad som ikke er journalført feiler`() {
            every { søknadRepository.findByIdOrNull("UUID") } returns søknad()

            assertThrows<IllegalStateException> { søknadskvitteringService.reduserSøknad("UUID") }
        }

        @Test
        fun `sletter søknadPdf, dokumentasjonsbehov og vedlegg for gitt søknadId`() {
            val søknadTilReduksjon = søknad(søknadPdf = EncryptedFile(ByteArray(20)), journalpostId = "321321")
            every { søknadRepository.findByIdOrNull("UUID") } returns søknadTilReduksjon
            every { søknadRepository.update(any()) } returns søknadTilReduksjon

            søknadskvitteringService.reduserSøknad("UUID")

            verify { søknadRepository.update(søknadTilReduksjon.copy(søknadPdf = null)) }
            verify { vedleggRepository.deleteBySøknadId("UUID") }
            verify { dokumentasjonsbehovRepository.deleteById("UUID") }
        }
    }

    @Nested
    inner class SlettSøknad {
        @Test
        fun `for søknad som ikke er journalført feiler`() {
            every { søknadRepository.findByIdOrNull("UUID") } returns søknad()

            assertThrows<IllegalStateException> { søknadskvitteringService.slettSøknad("UUID") }
        }

        @Test
        fun `sletter søknad for gitt søknadId`() {
            val søknadTilSletting = søknad(søknadPdf = EncryptedFile(ByteArray(20)), journalpostId = "321321")
            every { søknadRepository.findByIdOrNull("UUID") } returns søknadTilSletting

            søknadskvitteringService.slettSøknad("UUID")

            verify {
                søknadRepository.deleteById("UUID")
            }
        }
    }
}
