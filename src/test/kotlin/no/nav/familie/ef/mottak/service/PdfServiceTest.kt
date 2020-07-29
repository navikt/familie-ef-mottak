package no.nav.familie.ef.mottak.service

import io.mockk.*
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.integration.PdfClient
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.Fil
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import no.nav.familie.ef.mottak.service.Testdata.vedlegg
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.util.*

internal class PdfServiceTest {

    private val soknadRepository: SoknadRepository = mockk()
    private val vedleggRepository: VedleggRepository = mockk()
    private val pdfClient: PdfClient = mockk()
    private val pdfService: PdfService = PdfService(soknadRepository, vedleggRepository, pdfClient)

    private val serializedSoknad = createValidSøknadJson()
    private val pdf = Fil("321".toByteArray())
    private val søknad = Soknad(id = "randomUUID",
                                søknadJson = serializedSoknad,
                                søknadPdf = null,
                                fnr = "654",
                                dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                                journalpostId = null,
                                saksnummer = null)

    @BeforeEach
    private fun init() {
        søknadsRepositoryVilReturnere(søknad)
        every {
            vedleggRepository.findBySøknadId("søknadsId")
        } returns vedlegg.map { Vedlegg(UUID.randomUUID(), "søknadId", it.navn, it.tittel, Fil(byteArrayOf(12))) }
        pdfClientVilReturnere(pdf)
    }

    @Test
    fun `Søknad skal oppdateres med pdf når pdf genereres`() {
        // Given
        val slot = slot<Soknad>()
        capturePdfAddedToSøknad(slot)
        // When
        pdfService.lagPdf("søknadsId")
        // Then
        assertThat(pdf).isEqualTo(slot.captured.søknadPdf)
    }

    @Test
    fun `Søknad med pdf skal lagres`() {
        // Given
        val slot = slot<Soknad>()
        capturePdfAddedToSøknad(slot)
        // When
        pdfService.lagPdf("søknadsId")
        // Then
        verify(exactly = 1) { vedleggRepository.findBySøknadId(any()) }
        verify(exactly = 1) {
            soknadRepository.saveAndFlush(slot.captured)
        }
    }

    private fun pdfClientVilReturnere(pdf: Fil) {
        every {
            pdfClient.lagPdf(any())
        } returns pdf
    }

    private fun søknadsRepositoryVilReturnere(søknad: Soknad) {
        every {
            soknadRepository.findByIdOrNull("søknadsId")
        } returns søknad
    }

    private fun createValidSøknadJson(): String {
        val søknadDto = Testdata.søknad
        return objectMapper.writeValueAsString(søknadDto)
    }

    private fun capturePdfAddedToSøknad(slot: CapturingSlot<Soknad>) {
        every {
            soknadRepository.saveAndFlush(capture(slot))
        } answers {
            slot.captured
        }
    }

}