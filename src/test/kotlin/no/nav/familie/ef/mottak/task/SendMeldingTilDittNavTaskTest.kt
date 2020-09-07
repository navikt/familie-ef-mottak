package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.service.DittNavKafkaProducer
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.kontrakter.ef.søknad.Dokument
import no.nav.familie.kontrakter.ef.søknad.Dokumentasjonsbehov
import no.nav.familie.kontrakter.ef.søknad.SøknadType
import no.nav.familie.kontrakter.ef.søknad.dokumentasjonsbehov.DokumentasjonsbehovDto
import no.nav.familie.prosessering.domene.Task
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

internal class SendMeldingTilDittNavTaskTest {

    private lateinit var sendMeldingTilDittNavTask: SendMeldingTilDittNavTask
    private lateinit var dittNavKafkaProducer: DittNavKafkaProducer
    private lateinit var søknadService: SøknadService

    @BeforeEach
    internal fun setUp() {
        dittNavKafkaProducer = mockk(relaxed = true)
        søknadService = mockk()
        sendMeldingTilDittNavTask = SendMeldingTilDittNavTask(dittNavKafkaProducer, søknadService, mockk(relaxed = true))
    }

    @Test
    internal fun `overgangsstønad riktige intergasjoner`() {
        mockSøknad()
        mockDokumentasjonsbehov()

        sendMeldingTilDittNavTask.doTask(Task.nyTask("", SØKNAD_ID))

        verify(exactly = 1) {
            søknadService.get(any())
            søknadService.hentDokumentasjonsbehovForSøknad(any())
            dittNavKafkaProducer.sendToKafka(FNR, any(), any(), any(), any())
        }
    }

    @Test
    internal fun `overgangsstønad - uten dokumentasjonsbehov`() {
        testOgVerifiserMelding(emptyList(), "Vi har mottatt søknaden din om overgangsstønad.")
    }

    @Test
    internal fun `overgangsstønad - har allerede sendt inn`() {
        testOgVerifiserMelding(listOf(Dokumentasjonsbehov("", "", true, emptyList())),
                               "Vi har mottatt søknaden din om overgangsstønad. Se vedleggene du lastet opp.")
    }

    @Test
    internal fun `overgangsstønad - mangler vedlegg`() {
        testOgVerifiserMelding(listOf(Dokumentasjonsbehov("", "", false, emptyList())),
                               "Det ser ut til at det mangler noen vedlegg til søknaden din om overgangsstønad. Se hva som mangler og last opp vedlegg.")
    }

    @Test
    internal fun `overgangsstønad - har sendt inn vedlegg`() {
        testOgVerifiserMelding(listOf(Dokumentasjonsbehov("", "", false, listOf(Dokument("", "fil.pdf")))),
                               "Vi har mottatt søknaden din om overgangsstønad. Se vedleggene du lastet opp.")
    }

    private fun testOgVerifiserMelding(dokumentasjonsbehov: List<Dokumentasjonsbehov>,
                                       forventetMelding: String) {
        mockSøknad()
        mockDokumentasjonsbehov(dokumentasjonsbehov)

        sendMeldingTilDittNavTask.doTask(Task.nyTask("", SØKNAD_ID))

        verify(exactly = 1) {
            dittNavKafkaProducer.sendToKafka(eq(FNR), eq(forventetMelding), any(), any(), any())
        }
    }

    @Test
    internal fun `arbeidssøker skal ikke sjekke dokumentasjonsrepository`() {
        mockSøknad(søknadType = SøknadType.OVERGANGSSTØNAD_ARBEIDSSØKER)

        sendMeldingTilDittNavTask.doTask(Task.nyTask("", SØKNAD_ID))

        verify(exactly = 1) {
            søknadService.get(any())
            dittNavKafkaProducer.sendToKafka(FNR, any(), any(), any(), any())
        }
        verify(exactly = 0) {
            søknadService.hentDokumentasjonsbehovForSøknad(any())
        }
    }

    private fun mockDokumentasjonsbehov(dokumentasjonsbehov: List<Dokumentasjonsbehov> = emptyList(),
                                        søknadType: SøknadType = SøknadType.OVERGANGSSTØNAD) {
        if (søknadType == SøknadType.OVERGANGSSTØNAD_ARBEIDSSØKER) {
            error("Lagrer aldri dokumentasjonsbehov til arbeidssøker")
        }
        every { søknadService.hentDokumentasjonsbehovForSøknad(UUID.fromString(SØKNAD_ID)) } returns
                DokumentasjonsbehovDto(dokumentasjonsbehov, LocalDateTime.now(), søknadType, FNR)
    }

    private fun mockSøknad(søknadType: SøknadType = SøknadType.OVERGANGSSTØNAD) {
        every { søknadService.get(SØKNAD_ID) } returns
                Soknad(id = SØKNAD_ID,
                       søknadJson = "",
                       dokumenttype = søknadType.dokumentType,
                       fnr = FNR)
    }

    companion object {

        private const val SØKNAD_ID = "e8703be6-eb47-476a-ae52-096df47430d6"
        private const val FNR = "12345678901"
    }
}
