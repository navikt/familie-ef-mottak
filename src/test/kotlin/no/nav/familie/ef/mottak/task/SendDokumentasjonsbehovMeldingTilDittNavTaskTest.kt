package no.nav.familie.ef.mottak.task

import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.config.EttersendingConfig
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.DittNavKafkaProducer
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.kontrakter.ef.søknad.Dokument
import no.nav.familie.kontrakter.ef.søknad.Dokumentasjonsbehov
import no.nav.familie.kontrakter.ef.søknad.SøknadType
import no.nav.familie.kontrakter.ef.søknad.dokumentasjonsbehov.DokumentasjonsbehovDto
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import java.time.LocalDateTime
import java.util.Properties
import java.util.UUID

internal class SendDokumentasjonsbehovMeldingTilDittNavTaskTest {

    private lateinit var sendDokumentasjonsbehovMeldingTilDittNavTask: SendDokumentasjonsbehovMeldingTilDittNavTask
    private lateinit var dittNavKafkaProducer: DittNavKafkaProducer
    private lateinit var søknadService: SøknadService
    private lateinit var taskService: TaskService
    private lateinit var featureToggleService: FeatureToggleService
    private lateinit var ettersendingConfig: EttersendingConfig

    private val properties = Properties().apply { this["eventId"] = UUID.fromString(EVENT_ID) }

    @BeforeEach
    internal fun setUp() {
        dittNavKafkaProducer = mockk(relaxed = true)
        søknadService = mockk()
        taskService = mockk(relaxed = true)
        featureToggleService = mockk()
        ettersendingConfig = mockk()
        sendDokumentasjonsbehovMeldingTilDittNavTask =
            SendDokumentasjonsbehovMeldingTilDittNavTask(
                dittNavKafkaProducer,
                søknadService,
                taskService,
                ettersendingConfig,
                featureToggleService,
            )

        every { featureToggleService.isEnabled(any()) } returns true
        every { ettersendingConfig.ettersendingUrl } returns URL("https://dummy-url.nav.no")
    }

    @Test
    internal fun `overgangsstønad riktige integrasjoner - skal sende melding`() {
        mockSøknad()

        val dokumentasjonsBehov =
            listOf(Dokumentasjonsbehov("Ditt Nav kall må ha dokumentasjonsBehov", "id", false, listOf()))
        mockDokumentasjonsbehov(dokumentasjonsBehov, SøknadType.OVERGANGSSTØNAD)

        sendDokumentasjonsbehovMeldingTilDittNavTask.doTask(Task("", SØKNAD_ID, properties))

        verify(exactly = 1) {
            søknadService.get(any())
            søknadService.hentDokumentasjonsbehovForSøknad(any())
            taskService.save(any())
            dittNavKafkaProducer.sendToKafka(FNR, any(), EVENT_ID, isNull(true))
        }
    }

    @Test
    internal fun `overgangsstønad uten dokumentasjonsbehov - skal ikke kalle sendToKafka`() {
        mockSøknad()
        mockDokumentasjonsbehov(emptyList(), SøknadType.OVERGANGSSTØNAD)
        sendDokumentasjonsbehovMeldingTilDittNavTask.doTask(Task("", SØKNAD_ID, properties))
        verify {
            dittNavKafkaProducer wasNot called
            taskService.save(any()) wasNot called
        }
    }

    @Test
    internal fun `overgangsstønad har allerede sendt inn - skal sende mottatt melding`() {
        testOgVerifiserMelding(
            listOf(Dokumentasjonsbehov("", "", true, emptyList())),
            "Vi har mottatt søknaden din om overgangsstønad.",
        )
        verify {
            taskService.save(any()) wasNot called
        }
    }

    @Test
    internal fun `overgangsstønad mangler vedlegg - skal sende mangel melding`() {
        testOgVerifiserMelding(
            listOf(Dokumentasjonsbehov("", "", false, emptyList())),
            "Det ser ut til at det mangler noen vedlegg til søknaden din om overgangsstønad. " +
                "Se hva som mangler og last opp vedlegg.",
        )
        verify(exactly = 1) {
            taskService.save(any())
        }
    }

    @Test
    internal fun `overgangsstønad har sendt inn vedlegg - skal sende mottatt melding`() {
        testOgVerifiserMelding(
            listOf(Dokumentasjonsbehov("", "", false, listOf(Dokument("", "fil.pdf")))),
            "Vi har mottatt søknaden din om overgangsstønad.",
        )
        verify {
            taskService.save(any()) wasNot called
        }
    }

    @Test
    internal fun `arbeidssøker - skal ikke sende melding`() {
        mockSøknad(søknadType = SøknadType.OVERGANGSSTØNAD_ARBEIDSSØKER)

        sendDokumentasjonsbehovMeldingTilDittNavTask.doTask(Task("", SØKNAD_ID, properties))

        verify(exactly = 1) {
            søknadService.get(any())
        }
        verify {
            søknadService.hentDokumentasjonsbehovForSøknad(any()) wasNot called
            dittNavKafkaProducer wasNot called
            taskService.save(any()) wasNot called
        }
    }

    private fun testOgVerifiserMelding(
        dokumentasjonsbehov: List<Dokumentasjonsbehov>,
        forventetMelding: String,
        link: URL? = null,
    ) {
        mockSøknad()
        mockDokumentasjonsbehov(dokumentasjonsbehov, SøknadType.OVERGANGSSTØNAD)

        sendDokumentasjonsbehovMeldingTilDittNavTask.doTask(Task("", SØKNAD_ID, properties))

        verify(exactly = 1) {
            dittNavKafkaProducer.sendToKafka(eq(FNR), eq(forventetMelding), eq(EVENT_ID), link ?: any())
        }
    }

    private fun mockDokumentasjonsbehov(
        dokumentasjonsbehov: List<Dokumentasjonsbehov> = emptyList(),
        søknadType: SøknadType,
    ) {
        if (søknadType == SøknadType.OVERGANGSSTØNAD_ARBEIDSSØKER) {
            error("Lagrer aldri dokumentasjonsbehov til arbeidssøker")
        }

        every { søknadService.hentDokumentasjonsbehovForSøknad(any()) } returns
            DokumentasjonsbehovDto(dokumentasjonsbehov, LocalDateTime.now(), søknadType, FNR)
    }

    private fun mockSøknad(søknadType: SøknadType = SøknadType.OVERGANGSSTØNAD) {
        every { søknadService.get(SØKNAD_ID) } returns
            Søknad(
                id = SØKNAD_ID,
                søknadJson = EncryptedString(""),
                dokumenttype = søknadType.dokumentType,
                fnr = FNR,
            )
    }

    companion object {

        private const val SØKNAD_ID = "e8703be6-eb47-476a-ae52-096df47430d6"
        private const val EVENT_ID = "e8703be6-eb47-476a-ae52-096df47430d7"
        private const val FNR = "12345678901"
    }
}
