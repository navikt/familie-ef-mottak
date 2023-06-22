package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.task

import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import no.nav.familie.ef.mottak.config.EttersendingConfig
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.integration.SaksbehandlingClient
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.DittNavKafkaProducer
import no.nav.familie.ef.mottak.service.EttersendingService
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.ef.mottak.task.SendPåminnelseOmDokumentasjonsbehovTilDittNavTask
import no.nav.familie.ef.mottak.util.lagMeldingPåminnelseManglerDokumentasjonsbehov
import no.nav.familie.kontrakter.ef.søknad.SøknadType
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.prosessering.domene.Task
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import java.time.LocalDateTime
import java.util.Properties
import java.util.UUID

internal class SendPåminnelseOmDokumentasjonsbehovTilDittNavTaskTest {

    private lateinit var sendPåminnelseOmDokumentasjonsbehovTilDittNavTask: SendPåminnelseOmDokumentasjonsbehovTilDittNavTask
    private lateinit var dittNavKafkaProducer: DittNavKafkaProducer
    private lateinit var søknadService: SøknadService
    private lateinit var ettersendingService: EttersendingService
    private lateinit var featureToggleService: FeatureToggleService
    private lateinit var ettersendingConfig: EttersendingConfig
    private lateinit var saksbehandlingClient: SaksbehandlingClient

    private val properties = Properties().apply { this["eventId"] = UUID.fromString(EVENT_ID) }

    @BeforeEach
    internal fun setUp() {
        dittNavKafkaProducer = mockk(relaxed = true)
        søknadService = mockk()
        ettersendingService = mockk()
        featureToggleService = mockk()
        ettersendingConfig = mockk()
        saksbehandlingClient = mockk()
        sendPåminnelseOmDokumentasjonsbehovTilDittNavTask =
            SendPåminnelseOmDokumentasjonsbehovTilDittNavTask(
                dittNavKafkaProducer,
                søknadService,
                ettersendingService,
                ettersendingConfig,
                saksbehandlingClient,
            )

        every { featureToggleService.isEnabled(any()) } returns true
        every { ettersendingConfig.ettersendingUrl } returns URL("https://dummy-url.nav.no")
    }

    @Test
    internal fun `ingen etterfølgende søknader, ingen innsendte ettersendinger - skal sende påminnelse`() {
        val søknadData = listOf(SøknadData(søknadIds.first(), SøknadType.OVERGANGSSTØNAD.dokumentType, LocalDateTime.now()))
        val forventetMelding =
            lagMeldingPåminnelseManglerDokumentasjonsbehov(ettersendingConfig.ettersendingUrl, "overgangsstønad")
        mockHentSøknad(søknadData.first())
        mockHentSøknaderForPerson(søknadData)
        mockHentEttersendingerForPerson()
        every { saksbehandlingClient.kanSendePåminnelseTilBruker(any()) } returns false

        sendPåminnelseOmDokumentasjonsbehovTilDittNavTask.doTask(Task("", søknadIds.first(), properties))

        verify(exactly = 1) {
            søknadService.get(any())
            søknadService.hentSøknaderForPerson(PersonIdent(FNR))
            ettersendingService.hentEttersendingerForPerson(PersonIdent(FNR))
            dittNavKafkaProducer.sendToKafka(FNR, eq(forventetMelding.melding), any(), EVENT_ID, forventetMelding.link, PreferertKanal.SMS)
        }
    }

    @Test
    internal fun `har etterfølgende søknad, ingen innsendte ettersendinger - skal ikke sende påminnelse`() {
        val søknadData = listOf(
            SøknadData(søknadIds.first(), SøknadType.OVERGANGSSTØNAD.dokumentType, LocalDateTime.now()),
            SøknadData(søknadIds.get(1), SøknadType.BARNETILSYN.dokumentType, LocalDateTime.now().plusDays(1)),
        )
        mockHentSøknad(søknadData.first())
        mockHentSøknaderForPerson(søknadData)
        mockHentEttersendingerForPerson()
        every { saksbehandlingClient.kanSendePåminnelseTilBruker(any()) } returns true

        sendPåminnelseOmDokumentasjonsbehovTilDittNavTask.doTask(Task("", søknadIds.first(), properties))

        verify(exactly = 1) {
            søknadService.get(any())
            søknadService.hentSøknaderForPerson(PersonIdent(FNR))
            ettersendingService.hentEttersendingerForPerson(PersonIdent(FNR))
            dittNavKafkaProducer wasNot called
        }
    }

    @Test
    internal fun `ikke etterfølgende søknad, har innsendte ettersendinger - skal ikke sende påminnelse`() {
        val søknadData = listOf(SøknadData(søknadIds.first(), SøknadType.OVERGANGSSTØNAD.dokumentType, LocalDateTime.now()))
        mockHentSøknad(søknadData.first())
        mockHentSøknaderForPerson(søknadData)
        mockHentEttersendingerForPerson(LocalDateTime.now().plusHours(1))
        every { saksbehandlingClient.kanSendePåminnelseTilBruker(any()) } returns true

        sendPåminnelseOmDokumentasjonsbehovTilDittNavTask.doTask(Task("", søknadIds.first(), properties))

        verify(exactly = 1) {
            søknadService.get(any())
            søknadService.hentSøknaderForPerson(PersonIdent(FNR))
            ettersendingService.hentEttersendingerForPerson(PersonIdent(FNR))
            dittNavKafkaProducer wasNot called
        }
    }

    @Test
    internal fun `har etterfølgende søknad for arbeidssøker - skal sende påminnelse`() {
        val søknadData = listOf(
            SøknadData(søknadIds.first(), SøknadType.OVERGANGSSTØNAD.dokumentType, LocalDateTime.now()),
            SøknadData(søknadIds.get(1), SøknadType.OVERGANGSSTØNAD_ARBEIDSSØKER.dokumentType, LocalDateTime.now()),
        )
        val forventetMelding =
            lagMeldingPåminnelseManglerDokumentasjonsbehov(ettersendingConfig.ettersendingUrl, "overgangsstønad")
        mockHentSøknad(søknadData.first())
        mockHentSøknaderForPerson(søknadData)
        mockHentEttersendingerForPerson()
        every { saksbehandlingClient.kanSendePåminnelseTilBruker(any()) } returns false

        sendPåminnelseOmDokumentasjonsbehovTilDittNavTask.doTask(Task("", søknadIds.first(), properties))

        verify(exactly = 1) {
            søknadService.get(any())
            søknadService.hentSøknaderForPerson(PersonIdent(FNR))
            ettersendingService.hentEttersendingerForPerson(PersonIdent(FNR))
            dittNavKafkaProducer.sendToKafka(FNR, eq(forventetMelding.melding), any(), EVENT_ID, forventetMelding.link, PreferertKanal.SMS)
        }
    }

    @Test
    internal fun `har tidligere søknader, har tidligere ettersendinger - skal sende påminnelse`() {
        val søknadData = listOf(
            SøknadData(søknadIds.first(), SøknadType.OVERGANGSSTØNAD.dokumentType, LocalDateTime.now()),
            SøknadData(søknadIds.get(1), SøknadType.SKOLEPENGER.dokumentType, LocalDateTime.now().minusDays(1)),
        )
        val forventetMelding =
            lagMeldingPåminnelseManglerDokumentasjonsbehov(ettersendingConfig.ettersendingUrl, "overgangsstønad")
        mockHentSøknad(søknadData.first())
        mockHentSøknaderForPerson(søknadData)
        mockHentEttersendingerForPerson(LocalDateTime.now().minusHours(1))
        every { saksbehandlingClient.kanSendePåminnelseTilBruker(any()) } returns false

        sendPåminnelseOmDokumentasjonsbehovTilDittNavTask.doTask(Task("", søknadIds.first(), properties))

        verify(exactly = 1) {
            søknadService.get(any())
            søknadService.hentSøknaderForPerson(PersonIdent(FNR))
            ettersendingService.hentEttersendingerForPerson(PersonIdent(FNR))
            dittNavKafkaProducer.sendToKafka(FNR, eq(forventetMelding.melding), any(), EVENT_ID, forventetMelding.link, PreferertKanal.SMS)
        }
    }

    @Test
    internal fun `søknad har resultert i en påbegynt behandleSak oppgave - skal ikke sende påminnelse`() {
        val søknadData = listOf(SøknadData(søknadIds.first(), SøknadType.OVERGANGSSTØNAD.dokumentType, LocalDateTime.now()))
        mockHentSøknad(søknadData.first())
        mockHentSøknaderForPerson(søknadData)
        mockHentEttersendingerForPerson()
        every { saksbehandlingClient.kanSendePåminnelseTilBruker(any()) } returns true

        sendPåminnelseOmDokumentasjonsbehovTilDittNavTask.doTask(Task("", søknadIds.first(), properties))

        verify(exactly = 1) {
            søknadService.get(any())
            søknadService.hentSøknaderForPerson(PersonIdent(FNR))
            ettersendingService.hentEttersendingerForPerson(PersonIdent(FNR))
            dittNavKafkaProducer wasNot called
        }
    }

    @Test
    internal fun `stønadType er null - skal ikke sende påminnelse`() {
        val søknadData = listOf(SøknadData(søknadIds.first(), "", LocalDateTime.now()))
        mockHentSøknad(søknadData.first())
        mockHentSøknaderForPerson(søknadData)
        mockHentEttersendingerForPerson()
        every { saksbehandlingClient.kanSendePåminnelseTilBruker(any()) } returns false

        sendPåminnelseOmDokumentasjonsbehovTilDittNavTask.doTask(Task("", søknadIds.first(), properties))

        verify(exactly = 1) {
            søknadService.get(any())
            søknadService.hentSøknaderForPerson(PersonIdent(FNR))
            ettersendingService.hentEttersendingerForPerson(PersonIdent(FNR))
            dittNavKafkaProducer wasNot called
        }
    }

    private fun søknad(id: String, dokumenttype: String, opprettetTid: LocalDateTime) =
        Søknad(
            id = id,
            søknadJson = EncryptedString(""),
            dokumenttype = dokumenttype,
            fnr = FNR,
            opprettetTid = opprettetTid,
        )

    private fun mockHentSøknad(søknadData: SøknadData) {
        every { søknadService.get(søknadIds.first()) } returns søknad(
            søknadData.id,
            søknadData.dokumentType,
            søknadData.opprettetTid,
        )
    }

    private fun mockHentSøknaderForPerson(søknadData: List<SøknadData>) {
        every { søknadService.hentSøknaderForPerson(PersonIdent(FNR)) } returns
            listOf(søknad(søknadData.first().id, søknadData.first().dokumentType, søknadData.first().opprettetTid)) +
            søknadData.takeLast(søknadData.size - 1).map { søknad(it.id, it.dokumentType, it.opprettetTid) }
    }

    private fun ettersending(opprettetTid: LocalDateTime) =
        Ettersending(ettersendingJson = EncryptedString(""), stønadType = "OS", fnr = FNR, opprettetTid = opprettetTid)

    private fun mockHentEttersendingerForPerson(opprettetTid: LocalDateTime? = null) {
        every { ettersendingService.hentEttersendingerForPerson(PersonIdent(FNR)) } returns
            if (opprettetTid == null) emptyList() else listOf(ettersending(opprettetTid))
    }

    data class SøknadData(val id: String, val dokumentType: String, val opprettetTid: LocalDateTime)

    companion object {

        private const val EVENT_ID = "e8703be6-eb47-476a-ae52-096df47430d7"
        private const val FNR = "12345678901"

        private val søknadIds = listOf("s1", "s2", "s3", "s4")
    }
}
