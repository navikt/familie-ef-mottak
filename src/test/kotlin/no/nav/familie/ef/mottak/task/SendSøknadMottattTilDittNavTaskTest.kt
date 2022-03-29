package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.DittNavKafkaProducer
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.ef.mottak.task.SendDokumentasjonsbehovMeldingTilDittNavTask
import no.nav.familie.ef.mottak.task.SendSøknadMottattTilDittNavTask
import no.nav.familie.kontrakter.ef.søknad.SøknadType
import no.nav.familie.prosessering.domene.Task
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Properties
import java.util.UUID

internal class SendSøknadMottattTilDittNavTaskTest {

    private lateinit var sendSøknadMottattTilDittNavTask: SendSøknadMottattTilDittNavTask
    private lateinit var dittNavKafkaProducer: DittNavKafkaProducer
    private lateinit var søknadService: SøknadService
    private lateinit var task: Task

    @BeforeEach
    internal fun setUp() {
        dittNavKafkaProducer = mockk(relaxed = true)
        søknadService = mockk()
        sendSøknadMottattTilDittNavTask =
                SendSøknadMottattTilDittNavTask(dittNavKafkaProducer, søknadService)
        val properties = Properties().apply { this["eventId"] = UUID.fromString(EVENT_ID) }
        task = Task(
                payload = SØKNAD_ID,
                type = SendDokumentasjonsbehovMeldingTilDittNavTask.TYPE,
                properties = properties
        )
    }

    @Test
    internal fun `arbeidssøker skjema skal ha riktig tekst `() {
        mockSøknad(SøknadType.OVERGANGSSTØNAD_ARBEIDSSØKER)
        sendSøknadMottattTilDittNavTask.doTask(task)
        verifiserForventetKallMed("Vi har mottatt skjema enslig mor eller far som er arbeidssøker.")
    }

    @Test
    internal fun `Overgangsstønad skal ha riktig tekst `() {
        mockSøknad(SøknadType.OVERGANGSSTØNAD)
        sendSøknadMottattTilDittNavTask.doTask(task)
        verifiserForventetKallMed("Vi har mottatt søknaden din om overgangsstønad.")
    }

    @Test
    internal fun `Barnetilsyn skal ha riktig tekst `() {
        mockSøknad(SøknadType.BARNETILSYN)
        sendSøknadMottattTilDittNavTask.doTask(task)
        verifiserForventetKallMed("Vi har mottatt søknaden din om stønad til barnetilsyn.")
    }

    @Test
    internal fun `Skolepenger skal ha riktig tekst `() {
        mockSøknad(SøknadType.SKOLEPENGER)
        sendSøknadMottattTilDittNavTask.doTask(task)
        verifiserForventetKallMed("Vi har mottatt søknaden din om stønad til skolepenger.")
    }

    private fun verifiserForventetKallMed(forventetTekst: String) {
        verify(exactly = 1) {
            søknadService.get(any())
            dittNavKafkaProducer.sendToKafka(
                    FNR,
                    forventetTekst,
                    task.payload,
                    EVENT_ID,
                    null
            )
        }
    }

    private fun mockSøknad(søknadType: SøknadType) {
        every { søknadService.get(SØKNAD_ID) } returns
                Søknad(
                        id = SØKNAD_ID,
                        søknadJson = EncryptedString(""),
                        dokumenttype = søknadType.dokumentType,
                        fnr = FNR
                )
    }

    companion object {

        private const val SØKNAD_ID = "e8703be6-eb47-476a-ae52-096df47430d6"
        private const val EVENT_ID = "e8703be6-eb47-476a-ae52-096df47430d7"
        private const val FNR = "12345678901"
    }
}
