package no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.ef.mottak.repository.DokumentasjonsbehovRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.EncryptedFile
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.kontrakter.ef.søknad.Dokumentasjonsbehov
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.søknad.SistInnsendtSøknadDto
import no.nav.familie.kontrakter.felles.søknad.nyereEnn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class SøknadServiceTest {
    private val søknadRepository = mockk<SøknadRepository>(relaxed = true)
    private val vedleggRepository = mockk<VedleggRepository>(relaxed = true)
    private val dokumentasjonsbehovRepository = mockk<DokumentasjonsbehovRepository>(relaxed = true)
    private val taskProsesseringService = mockk<TaskProsesseringService>(relaxed = true)

    private val søknadService =
        SøknadService(søknadRepository, vedleggRepository, mockk(), dokumentasjonsbehovRepository, taskProsesseringService)

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

        assertThat(søknadService.hentDokumentasjonsbehovForPerson(fnr)).hasSize(3)
    }

    @Nested
    inner class ReduserSøknad {
        @Test
        fun `for søknad som ikke er journalført feiler`() {
            every { søknadRepository.findByIdOrNull("UUID") } returns søknad()

            assertThrows<IllegalStateException> { søknadService.reduserSøknad("UUID") }
        }

        @Test
        fun `sletter søknadPdf, dokumentasjonsbehov og vedlegg for gitt søknadId`() {
            val søknadTilReduksjon = søknad(søknadPdf = EncryptedFile(ByteArray(20)), journalpostId = "321321")
            every { søknadRepository.findByIdOrNull("UUID") } returns søknadTilReduksjon
            every { søknadRepository.update(any()) } returns søknadTilReduksjon

            søknadService.reduserSøknad("UUID")

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

            assertThrows<IllegalStateException> { søknadService.slettSøknad("UUID") }
        }

        @Test
        fun `sletter søknad for gitt søknadId`() {
            val søknadTilSletting = søknad(søknadPdf = EncryptedFile(ByteArray(20)), journalpostId = "321321")
            every { søknadRepository.findByIdOrNull("UUID") } returns søknadTilSletting

            søknadService.slettSøknad("UUID")

            verify {
                søknadRepository.deleteById("UUID")
            }
        }
    }

    @Nested
    inner class HentSistInnsendteSøknadPerStønad {
        val personIdent = "12345678910"

        @Test
        fun `skal returnere tom liste for førstegangsøker`() {
            every { søknadRepository.finnSisteSøknadForPersonOgStønadstype(any(), any()) } returns null

            val søknader = søknadService.hentSistInnsendtSøknadPerStønad(personIdent)

            assertThat(søknader).isEmpty()
        }

        @Test
        fun `skal returnere tom liste med søkander som er eldre enn 30 dager`() {
            val overgangStønadSøknad =
                Søknad(
                    søknadJson = EncryptedString(""),
                    dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                    fnr = personIdent,
                    opprettetTid = LocalDateTime.now().minusDays(40),
                )

            val barnetilsynSøknad =
                Søknad(
                    søknadJson = EncryptedString(""),
                    dokumenttype = DOKUMENTTYPE_BARNETILSYN,
                    fnr = personIdent,
                    opprettetTid = LocalDateTime.now().minusDays(41),
                )

            every { søknadRepository.finnSisteSøknadForPersonOgStønadstype(personIdent, DOKUMENTTYPE_OVERGANGSSTØNAD) } returns overgangStønadSøknad
            every { søknadRepository.finnSisteSøknadForPersonOgStønadstype(personIdent, DOKUMENTTYPE_BARNETILSYN) } returns barnetilsynSøknad
            every { søknadRepository.finnSisteSøknadForPersonOgStønadstype(personIdent, DOKUMENTTYPE_SKOLEPENGER) } returns null

            val søknader = søknadService.hentSistInnsendtSøknadPerStønad(personIdent)

            assertThat(søknader).isEmpty()
        }

        @Test
        fun `skal returnere liste med søknader som er innsendt innen 30 dager`() {
            val overgangStønadSøknad =
                Søknad(
                    søknadJson = EncryptedString(""),
                    dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                    fnr = personIdent,
                    opprettetTid = LocalDateTime.now().minusDays(25),
                )

            val barnetilsynSøknad =
                Søknad(
                    søknadJson = EncryptedString(""),
                    dokumenttype = DOKUMENTTYPE_BARNETILSYN,
                    fnr = personIdent,
                    opprettetTid = LocalDateTime.now().minusDays(7),
                )

            val skolepengerSøknad =
                Søknad(
                    søknadJson = EncryptedString(""),
                    dokumenttype = DOKUMENTTYPE_SKOLEPENGER,
                    fnr = personIdent,
                    opprettetTid = LocalDateTime.now().minusDays(6),
                )

            every { søknadRepository.finnSisteSøknadForPersonOgStønadstype(personIdent, DOKUMENTTYPE_OVERGANGSSTØNAD) } returns overgangStønadSøknad
            every { søknadRepository.finnSisteSøknadForPersonOgStønadstype(personIdent, DOKUMENTTYPE_BARNETILSYN) } returns barnetilsynSøknad
            every { søknadRepository.finnSisteSøknadForPersonOgStønadstype(personIdent, DOKUMENTTYPE_SKOLEPENGER) } returns skolepengerSøknad

            val søknader = søknadService.hentSistInnsendtSøknadPerStønad(personIdent)

            assertThat(søknader.size).isEqualTo(3)
        }

        @Test
        fun `skal filtrere søknader eldre enn 30 dager`() {
            val søknader =
                listOf(
                    SistInnsendtSøknadDto(
                        søknadsdato = LocalDate.now().minusDays(40),
                        stønadType = StønadType.OVERGANGSSTØNAD,
                    ),
                    SistInnsendtSøknadDto(
                        søknadsdato = LocalDate.now().minusDays(41),
                        stønadType = StønadType.BARNETILSYN,
                    ),
                    SistInnsendtSøknadDto(
                        søknadsdato = LocalDate.now().minusDays(2),
                        stønadType = StønadType.SKOLEPENGER,
                    ),
                )

            val filtrerteSøknader = søknader.filter { it.nyereEnn() }

            assertThat(filtrerteSøknader.size).isEqualTo(1)
        }
    }
}
