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

internal class SendDokumentasjonsbehovMeldingTilDittNavTaskTest {

    private lateinit var sendDokumentasjonsbehovMeldingTilDittNavTask: SendDokumentasjonsbehovMeldingTilDittNavTask
    private lateinit var dittNavKafkaProducer: DittNavKafkaProducer
    private lateinit var søknadService: SøknadService

    @BeforeEach
    internal fun setUp() {
        dittNavKafkaProducer = mockk(relaxed = true)
        søknadService = mockk()
        sendDokumentasjonsbehovMeldingTilDittNavTask =
                SendDokumentasjonsbehovMeldingTilDittNavTask(dittNavKafkaProducer, søknadService, mockk(relaxed = true))
    }

    @Test
    internal fun `overgangsstønad riktige intergasjoner`() {
        mockSøknad()

        val dokumentasjonsBehov =
                listOf(Dokumentasjonsbehov("Ditt Nav kall må ha dokumentasjonsBehov", "id", false, listOf()))
        mockDokumentasjonsbehov(dokumentasjonsBehov)

        sendDokumentasjonsbehovMeldingTilDittNavTask.doTask(Task.nyTask("", SØKNAD_ID))

        verify(exactly = 1) {
            søknadService.get(any())
            søknadService.hentDokumentasjonsbehovForSøknad(any())
            dittNavKafkaProducer.sendToKafka(FNR, any(), any(), any(), isNull(true))
        }
    }

    @Test
    internal fun `overgangsstønad - uten dokumentasjonsbehov skal ikke kalle sendToKafka`() {
        mockSøknad()
        mockDokumentasjonsbehov(emptyList())
        sendDokumentasjonsbehovMeldingTilDittNavTask.doTask(Task.nyTask("", SØKNAD_ID))
        verify(exactly = 0) {
            dittNavKafkaProducer.sendToKafka(any(),
                                             any(),
                                             any(),
                                             any(),
                                             any())
        }
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

    @Test
    internal fun `arbeidssøker skal ikke sjekke dokumentasjonsrepository`() {
        mockSøknad(søknadType = SøknadType.OVERGANGSSTØNAD_ARBEIDSSØKER)
        mockDokumentasjonsbehov(emptyList())

        sendDokumentasjonsbehovMeldingTilDittNavTask.doTask(Task.nyTask("", SØKNAD_ID))

        verify(exactly = 1) {
            søknadService.get(any())
            søknadService.hentDokumentasjonsbehovForSøknad(any())
        }
        verify(exactly = 0) {
            dittNavKafkaProducer.sendToKafka(FNR,
                                             "Vi har mottatt skjema enslig mor eller far som er arbeidssøker.",
                                             any(),
                                             any(),
                                             "")

        }
    }

    private fun testOgVerifiserMelding(dokumentasjonsbehov: List<Dokumentasjonsbehov>,
                                       forventetMelding: String,
                                       link: String? = null) {
        mockSøknad()
        mockDokumentasjonsbehov(dokumentasjonsbehov)

        sendDokumentasjonsbehovMeldingTilDittNavTask.doTask(Task.nyTask("", SØKNAD_ID))

        verify(exactly = 1) {
            dittNavKafkaProducer.sendToKafka(eq(FNR), eq(forventetMelding), any(), any(), link ?: any())
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
