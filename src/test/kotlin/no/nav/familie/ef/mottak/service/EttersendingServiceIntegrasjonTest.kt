package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.EttersendingVedleggRepository
import no.nav.familie.ef.mottak.repository.domain.EncryptedFile
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg
import no.nav.familie.ef.mottak.repository.util.findByIdOrThrow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class EttersendingServiceIntegrasjonTest : IntegrasjonSpringRunnerTest() {
    @Autowired
    lateinit var ettersendingVedleggRepository: EttersendingVedleggRepository

    @Autowired
    lateinit var ettersendingRepository: EttersendingRepository

    @Autowired
    lateinit var ettersendingService: EttersendingService

    @Test
    fun `skal splitte opp ettersendingsvedlegg fra en ettersending`() {
        val opprinneligEttersending =
            Ettersending(
                ettersendingJson = EncryptedString("json"),
                ettersendingPdf = EncryptedFile("json".toByteArray()),
                fnr = "12345678901",
                stønadType = "OVERGANGSSTØNAD",
                taskOpprettet = true,
            )
        ettersendingRepository.insert(opprinneligEttersending)
        val vedlegg1 = lagVedlegg(opprinneligEttersending, "Første dokument")
        val vedlegg2 = lagVedlegg(opprinneligEttersending, "Andre dokument")
        val vedlegg3 = lagVedlegg(opprinneligEttersending, "Tredje dokument")
        ettersendingVedleggRepository.insertAll(listOf(vedlegg1, vedlegg2, vedlegg3))

        val nyEttersendingId = ettersendingService.trekkUtEttersendingTilEgenTaskForVedlegg(vedlegg2.id)

        val nyEttersending = ettersendingRepository.findByIdOrThrow(nyEttersendingId)
        assertThat(nyEttersending.taskOpprettet).isFalse

        val vedleggPåNyEttersending = ettersendingVedleggRepository.findByEttersendingId(nyEttersendingId)
        assertThat(vedleggPåNyEttersending).hasSize(1)
        assertThat(vedleggPåNyEttersending.first().id).isEqualTo(vedlegg2.id)

        val vedleggPåOpprinneligEttersending = ettersendingVedleggRepository.findByEttersendingId(opprinneligEttersending.id)
        assertThat(vedleggPåOpprinneligEttersending).hasSize(2)
        assertThat(vedleggPåOpprinneligEttersending.map { it.id }).contains(vedlegg1.id, vedlegg3.id)
    }

    private fun lagVedlegg(
        opprinneligEttersending: Ettersending,
        dokumentnavn: String,
    ) = EttersendingVedlegg(
        id = UUID.randomUUID(),
        ettersendingId = opprinneligEttersending.id,
        navn = dokumentnavn,
        tittel = dokumentnavn,
        innhold = EncryptedFile(bytes = "noe".toByteArray()),
    )
}
