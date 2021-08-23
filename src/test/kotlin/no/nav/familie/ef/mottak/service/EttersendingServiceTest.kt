package no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.mottak.integration.FamilieDokumentClient
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.EttersendingVedleggRepository
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg
import no.nav.familie.kontrakter.ef.ettersending.EttersendingDto
import no.nav.familie.kontrakter.ef.ettersending.EttersendingMedVedlegg
import no.nav.familie.kontrakter.ef.ettersending.EttersendingUtenSøknad
import no.nav.familie.kontrakter.ef.ettersending.Innsending
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.søknad.Dokument
import no.nav.familie.kontrakter.ef.søknad.Innsendingsdetaljer
import no.nav.familie.kontrakter.ef.søknad.Søknadsfelt
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

internal class EttersendingServiceTest {

    private val ettersendingRepository = mockk<EttersendingRepository>()
    private val ettersendingVedleggRepository = mockk<EttersendingVedleggRepository>()
    private val dokumentClient = mockk<FamilieDokumentClient>()

    private val ettersendingService = EttersendingService(ettersendingRepository = ettersendingRepository,
                                                          ettersendingVedleggRepository = ettersendingVedleggRepository,
                                                          dokumentClient = dokumentClient)

    @Test
    internal fun `skal motta ettersending og lagre dette ned`() {
        val innsendingsdetaljer = Søknadsfelt("detaljer", Innsendingsdetaljer(Søknadsfelt("dato", LocalDateTime.now())))
        val dokument1 = "1234".toByteArray()
        val dokument2 = "999111".toByteArray()
        val vedlegg1 = Vedlegg(UUID.randomUUID().toString(), "Vedlegg 1", "Vedleggtittel 1")
        val vedlegg2 = Vedlegg(UUID.randomUUID().toString(), "Vedlegg 2", "Vedleggtittel 2")
        val innsending1 = Innsending(beskrivelse = "Lærlingekontrakt",
                                     dokumenttype = "DOKUMENTASJON_LÆRLING",
                                     vedlegg = listOf(Dokument(vedlegg1.id, vedlegg1.navn)))
        val innsending2 = Innsending(beskrivelse = "Dokumentasjon på at du ikke kan ta arbeid",
                                     dokumenttype = "DOKUMENTASJON_IKKE_VILLIG_TIL_ARBEID",
                                     vedlegg = listOf(Dokument(vedlegg2.id, vedlegg2.navn)))
        val ettersendingUtenSøknad = EttersendingUtenSøknad(innsending = listOf(innsending1, innsending2)
        )
        val ettersendingDto = EttersendingDto("12345678901", StønadType.OVERGANGSSTØNAD, null, ettersendingUtenSøknad)
        val ettersendingMedVedlegg = EttersendingMedVedlegg(
                innsendingsdetaljer = innsendingsdetaljer,
                vedlegg = listOf(vedlegg1, vedlegg2),
                ettersending = ettersendingDto

        )

        val ettersendingSlot = slot<Ettersending>()
        val ettersendingVedleggSlot = slot<List<EttersendingVedlegg>>()
        every {
            ettersendingRepository.save(capture(ettersendingSlot))
        } answers { ettersendingSlot.captured}

        every {
            ettersendingVedleggRepository.saveAll(capture(ettersendingVedleggSlot))
        } answers { ettersendingVedleggSlot.captured}


        ettersendingService.mottaEttersending(ettersending = ettersendingMedVedlegg,
                                              vedlegg = mapOf(vedlegg1.id to dokument1, vedlegg2.id to dokument2))


        assertThat(ettersendingSlot.captured.stønadType).isEqualTo(StønadType.OVERGANGSSTØNAD.toString())
        assertThat(ettersendingSlot.captured.ettersendingPdf).isNull()
        assertThat(ettersendingSlot.captured.fnr).isEqualTo(ettersendingDto.fnr)
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