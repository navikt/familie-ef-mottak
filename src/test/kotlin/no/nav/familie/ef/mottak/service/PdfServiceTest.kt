package no.nav.familie.ef.mottak.service

import io.mockk.*
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.ef.mottak.integration.PdfClient
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.Fil
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.service.Testdata.vedlegg
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull

internal class PdfServiceTest {

    private val soknadRepository: SoknadRepository = mockk()
    private val vedleggRepository: VedleggRepository = mockk()
    private val pdfClient: PdfClient = mockk()
    private val pdfService: PdfService = PdfService(soknadRepository, vedleggRepository, pdfClient)

    private val pdf = Fil("321".toByteArray())
    private val søknadOvergangsstønadId = "søknadOvergangsstønadId"
    private val søknadOvergangsstønad = Soknad(id = søknadOvergangsstønadId,
                                               søknadJson = createValidSøknadJson(Testdata.søknadOvergangsstønad),
                                               søknadPdf = null,
                                               fnr = "654",
                                               dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                                               journalpostId = null,
                                               saksnummer = null)

    private val søknadSkolepengerId = "søknadSkolepengerId"
    private val søknadSkolepenger = Soknad(id = søknadSkolepengerId,
                                           søknadJson = createValidSøknadJson(Testdata.søknadSkolepenger),
                                           søknadPdf = null,
                                           fnr = "654",
                                           dokumenttype = DOKUMENTTYPE_SKOLEPENGER,
                                           journalpostId = null,
                                           saksnummer = null)

    private val søknadBarnetilsynId = "søknadBarnetilsynId"
    private val søknadBarnetilsyn = Soknad(id = søknadBarnetilsynId,
                                           søknadJson = createValidSøknadJson(Testdata.søknadBarnetilsyn),
                                           søknadPdf = null,
                                           fnr = "654",
                                           dokumenttype = DOKUMENTTYPE_BARNETILSYN,
                                           journalpostId = null,
                                           saksnummer = null)


    @BeforeEach
    private fun init() {
        søknadsRepositoryVilReturnere(søknadOvergangsstønad, søknadBarnetilsyn, søknadSkolepenger)
        every {
            vedleggRepository.findTitlerBySøknadId(any())
        } returns vedlegg.map { it.tittel }
        pdfClientVilReturnere(pdf)
    }

    @Test
    fun `Søknad skal oppdateres med pdf når pdf genereres`() {
        // Given
        val slot = slot<Soknad>()
        capturePdfAddedToSøknad(slot)
        // When
        pdfService.lagPdf(søknadOvergangsstønadId)
        // Then
        assertThat(pdf).isEqualTo(slot.captured.søknadPdf)
    }

    @Test
    fun `Skolepengesøknad skal oppdateres med pdf når pdf genereres`() {
        // Given
        val slot = slot<Soknad>()
        capturePdfAddedToSøknad(slot)
        // When
        pdfService.lagPdf(søknadSkolepengerId)
        // Then
        assertThat(pdf).isEqualTo(slot.captured.søknadPdf)
    }

    @Test
    fun `Søknad med pdf skal lagres`() {
        // Given
        val slot = slot<Soknad>()
        capturePdfAddedToSøknad(slot)
        // When
        pdfService.lagPdf(søknadBarnetilsynId)
        // Then
        verify(exactly = 1) { vedleggRepository.findTitlerBySøknadId(any()) }
        verify(exactly = 1) {
            soknadRepository.saveAndFlush(slot.captured)
        }
    }

    private fun pdfClientVilReturnere(pdf: Fil) {
        every {
            pdfClient.lagPdf(any())
        } returns pdf
    }

    private fun søknadsRepositoryVilReturnere(vararg søknad: Soknad) {
        søknad.forEach {
            every {
                soknadRepository.findByIdOrNull(it.id)
            } returns it
        }
    }

    private fun createValidSøknadJson(søknad: Any): String {
        return objectMapper.writeValueAsString(søknad)
    }

    private fun capturePdfAddedToSøknad(slot: CapturingSlot<Soknad>) {
        every {
            soknadRepository.saveAndFlush(capture(slot))
        } answers {
            slot.captured
        }
    }

}