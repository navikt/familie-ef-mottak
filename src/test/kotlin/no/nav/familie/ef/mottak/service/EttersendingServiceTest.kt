package no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.mockapi.mockFamilieDokumentClient
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.EttersendingVedleggRepository
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg
import no.nav.familie.kontrakter.ef.ettersending.Dokumentasjonsbehov
import no.nav.familie.kontrakter.ef.ettersending.EttersendelseDto
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class EttersendingServiceTest {

    private val ettersendingRepository = mockk<EttersendingRepository>()
    private val ettersendingVedleggRepository = mockk<EttersendingVedleggRepository>()
    private val dokumentClient = mockFamilieDokumentClient()

    private val ettersendingService = EttersendingService(ettersendingRepository = ettersendingRepository,
                                                          ettersendingVedleggRepository = ettersendingVedleggRepository,
                                                          dokumentClient = dokumentClient)

    private val dokument1 = "1234".toByteArray()
    private val dokument2 = "999111".toByteArray()
    private val vedlegg1 = Vedlegg(UUID.randomUUID().toString(), "Vedlegg 1", "Vedleggtittel 1")
    private val vedlegg2 = Vedlegg(UUID.randomUUID().toString(), "Vedlegg 2", "Vedleggtittel 2")

    @BeforeEach
    internal fun setUp() {
        every { dokumentClient.hentVedlegg(vedlegg1.id) } returns dokument1
        every { dokumentClient.hentVedlegg(vedlegg2.id) } returns dokument2
    }

    @Test
    internal fun `skal motta ettersending og lagre dette ned`() {
        val dokument1 = "1234".toByteArray()
        val dokument2 = "999111".toByteArray()
        val vedlegg1 = Vedlegg(UUID.randomUUID().toString(), "Vedlegg 1", "Vedleggtittel 1")
        val vedlegg2 = Vedlegg(UUID.randomUUID().toString(), "Vedlegg 2", "Vedleggtittel 2")
        val dokumentasjonsbehov1 = Dokumentasjonsbehov(id = UUID.randomUUID().toString(),
                                                       søknadsdata = null,
                                                       dokumenttype = "DOKUMENTASJON_LÆRLING",
                                                       beskrivelse = "Lærlingekontrakt",
                                                       stønadType = StønadType.OVERGANGSSTØNAD,
                                                       innsendingstidspunkt = null,
                                                       vedlegg = listOf(vedlegg1))
        val dokumentasjonsbehov2 = Dokumentasjonsbehov(id = UUID.randomUUID().toString(),
                                                       søknadsdata = null,
                                                       dokumenttype = "DOKUMENTASJON_IKKE_VILLIG_TIL_ARBEID",
                                                       beskrivelse = "Dokumentasjon på at du ikke kan ta arbeid",
                                                       stønadType = StønadType.OVERGANGSSTØNAD,
                                                       innsendingstidspunkt = null,
                                                       vedlegg = listOf(vedlegg2))
        val fnr = "123456789010"
        val ettersendingSlot = slot<Ettersending>()
        val ettersendingVedleggSlot = slot<List<EttersendingVedlegg>>()
        every { dokumentClient.hentVedlegg(vedlegg1.id) } returns dokument1
        every { dokumentClient.hentVedlegg(vedlegg2.id) } returns dokument2
        every {
            ettersendingVedleggRepository.saveAll(capture(ettersendingVedleggSlot))
        } answers { ettersendingVedleggSlot.captured }
        every {
            hint(Ettersending::class)
            ettersendingRepository.save(capture(ettersendingSlot))
        } answers { ettersendingSlot.captured }

        ettersendingService.mottaEttersending(mapOf(StønadType.OVERGANGSSTØNAD to EttersendelseDto(listOf(dokumentasjonsbehov1,
                                                                                                          dokumentasjonsbehov2),
                                                                                                   fnr = fnr)))

        assertThat(ettersendingSlot.captured.stønadType).isEqualTo(StønadType.OVERGANGSSTØNAD.toString())
        assertThat(ettersendingSlot.captured.ettersendingPdf).isNull()
        assertThat(ettersendingSlot.captured.fnr).isEqualTo(fnr)
        assertThat(ettersendingSlot.captured.journalpostId).isNull()
        assertThat(ettersendingSlot.captured.taskOpprettet).isFalse

        assertThat(ettersendingVedleggSlot.captured[0].tittel).isEqualTo(vedlegg1.tittel)
        assertThat(ettersendingVedleggSlot.captured[0].ettersendingId).isEqualTo(ettersendingSlot.captured.id)
        assertThat(ettersendingVedleggSlot.captured[0].navn).isEqualTo(vedlegg1.navn)
        assertThat(ettersendingVedleggSlot.captured[0].innhold.bytes).isEqualTo(dokument1)
        assertThat(ettersendingVedleggSlot.captured[1].tittel).isEqualTo(vedlegg2.tittel)
        assertThat(ettersendingVedleggSlot.captured[1].ettersendingId).isEqualTo(ettersendingSlot.captured.id)
        assertThat(ettersendingVedleggSlot.captured[1].navn).isEqualTo(vedlegg2.navn)
        assertThat(ettersendingVedleggSlot.captured[1].innhold.bytes).isEqualTo(dokument2)

    }
}