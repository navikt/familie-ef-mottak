package no.nav.familie.ef.mottak.hendelse

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.HendelsesloggRepository
import no.nav.familie.ef.mottak.repository.domain.Hendelseslogg
import no.nav.familie.ef.mottak.task.LagJournalføringsoppgaveTask
import no.nav.familie.kontrakter.felles.journalpost.*
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.MDC
import org.springframework.kafka.support.Acknowledgment

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JournalføringHendelseServiceTest {

    @MockK
    lateinit var integrasjonerClient: IntegrasjonerClient

    @MockK(relaxed = true)
    lateinit var mockTaskRepository: TaskRepository

    @MockK(relaxed = true)
    lateinit var mockFeatureToggleService: FeatureToggleService

    @MockK(relaxed = true)
    lateinit var mockHendelsesloggRepository: HendelsesloggRepository

    @MockK(relaxed = true)
    lateinit var ack: Acknowledgment

    @InjectMockKs
    lateinit var service: JournalhendelseService

    @BeforeEach
    internal fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every {
            mockHendelsesloggRepository.save(any())
        } returns Hendelseslogg(offset = 1L, hendelseId = "")

        //Inngående papirsøknad, Mottatt
        every {
            integrasjonerClient.hentJournalpost(JOURNALPOST_PAPIRSØKNAD)
        } returns Journalpost(journalpostId = JOURNALPOST_PAPIRSØKNAD,
                              journalposttype = Journalposttype.I,
                              journalstatus = Journalstatus.MOTTATT,
                              bruker = Bruker("123456789012", BrukerIdType.AKTOERID),
                              tema = "ENF",
                              kanal = "SKAN_NETS",
                              behandlingstema = null,
                              dokumenter = null,
                              journalforendeEnhet = null,
                              sak = null)

        //Inngående digital, Mottatt
        every {
            integrasjonerClient.hentJournalpost(JOURNALPOST_DIGITALSØKNAD)
        } returns Journalpost(journalpostId = JOURNALPOST_DIGITALSØKNAD,
                              journalposttype = Journalposttype.I,
                              journalstatus = Journalstatus.MOTTATT,
                              bruker = Bruker("123456789012", BrukerIdType.AKTOERID),
                              tema = "ENF",
                              kanal = "NAV_NO")

        //Utgående digital, Mottatt
        every {
            integrasjonerClient.hentJournalpost(JOURNALPOST_UTGÅENDE_DOKUMENT)
        } returns Journalpost(journalpostId = JOURNALPOST_UTGÅENDE_DOKUMENT,
                              journalposttype = Journalposttype.U,
                              journalstatus = Journalstatus.MOTTATT,
                              bruker = Bruker("123456789012", BrukerIdType.AKTOERID),
                              tema = "ENF",
                              kanal = "SKAN_NETS")

        //Ikke barnetrygd
        every {
            integrasjonerClient.hentJournalpost(JOURNALPOST_IKKE_ENFNETRYGD)
        } returns Journalpost(journalpostId = JOURNALPOST_IKKE_ENFNETRYGD,
                              journalposttype = Journalposttype.U,
                              journalstatus = Journalstatus.MOTTATT,
                              bruker = Bruker("123456789012", BrukerIdType.AKTOERID),
                              tema = "FOR",
                              kanal = "NAV_NO")

        //ferdigstilt journalpost
        every {
            integrasjonerClient.hentJournalpost(JOURNALPOST_FERDIGSTILT)
        } returns Journalpost(journalpostId = JOURNALPOST_FERDIGSTILT,
                              journalposttype = Journalposttype.U,
                              journalstatus = Journalstatus.FERDIGSTILT,
                              bruker = Bruker("123456789012", BrukerIdType.AKTOERID),
                              tema = "FOR",
                              kanal = "NAV_NO")

        every { mockFeatureToggleService.isEnabled(any()) } returns true
    }

    @Test
    fun `Mottak av papirsøknader skal opprette OpprettOppgaveForJournalføringTask`() {
        MDC.put("callId", "papir")
        val record = opprettRecord(JOURNALPOST_PAPIRSØKNAD)

        service.behandleJournalhendelse(record)


        val taskSlot = slot<Task>()
        verify {
            mockTaskRepository.save(capture(taskSlot))
        }

        assertThat(taskSlot.captured).isNotNull
        assertThat(taskSlot.captured.payload).isEqualTo(JOURNALPOST_PAPIRSØKNAD)
        assertThat(taskSlot.captured.metadata.getProperty("callId")).isEqualTo("papir")
        assertThat(taskSlot.captured.type).isEqualTo(LagJournalføringsoppgaveTask.TYPE)
    }

    @Test
    fun `Hendelser hvor journalpost er alt FERDIGSTILT skal ignoreres`() {
        val record = opprettRecord(JOURNALPOST_FERDIGSTILT)

        service.behandleJournalhendelse(record)

        verify(exactly = 0) {
            mockTaskRepository.save(any())
        }

    }

    @Test
    fun `Utgående journalposter skal ignoreres`() {
        val record = opprettRecord(JOURNALPOST_UTGÅENDE_DOKUMENT)

        service.behandleJournalhendelse(record)

        verify(exactly = 0) {
            mockTaskRepository.save(any())
        }

    }


    @Test
    fun `Skal ignorere hendelse fordi den eksisterer i hendelseslogg`() {
        val consumerRecord = ConsumerRecord("topic", 1,
                                            OFFSET,
                                            42L, opprettRecord(JOURNALPOST_PAPIRSØKNAD))
        every {
            mockHendelsesloggRepository.existsByHendelseId("hendelseId")
        } returns true

        service.prosesserNyHendelse(consumerRecord, ack)

        verify { ack.acknowledge() }

        verify(exactly = 0) {
            mockHendelsesloggRepository.save(any())
        }
    }

    @Test
    fun `Mottak av gyldig hendelse skal delegeres til service`() {
        val consumerRecord = ConsumerRecord("topic", 1,
                                            OFFSET,
                                            42L, opprettRecord(JOURNALPOST_PAPIRSØKNAD))

        service.prosesserNyHendelse(consumerRecord, ack)

        verify { ack.acknowledge() }

        val slot = slot<Hendelseslogg>()
        verify(exactly = 1) {
            mockHendelsesloggRepository.save(capture(slot))
        }

        assertThat(slot.captured).isNotNull
        assertThat(slot.captured.offset).isEqualTo(OFFSET)
        assertThat(slot.captured.hendelseId).isEqualTo(HENDELSE_ID)
        assertThat(slot.captured.metadata["journalpostId"]).isEqualTo(JOURNALPOST_PAPIRSØKNAD)
        assertThat(slot.captured.metadata["hendelsesType"]).isEqualTo("MidlertidigJournalført")
    }

    @Test
    fun `Ikke gyldige hendelsetyper skal ignoreres`() {
        val ugyldigHendelsetypeRecord = opprettRecord(journalpostId = JOURNALPOST_PAPIRSØKNAD, hendelseType = "UgyldigType")
        val consumerRecord = ConsumerRecord("topic", 1,
                                            OFFSET,
                                            42L, ugyldigHendelsetypeRecord)

        service.prosesserNyHendelse(consumerRecord, ack)


        verify { ack.acknowledge() }
        val slot = slot<Hendelseslogg>()
        verify(exactly = 1) {
            mockHendelsesloggRepository.save(capture(slot))
        }
        assertThat(slot.captured).isNotNull
        assertThat(slot.captured.offset).isEqualTo(OFFSET)
        assertThat(slot.captured.hendelseId).isEqualTo(HENDELSE_ID)
        assertThat(slot.captured.metadata["journalpostId"]).isEqualTo(JOURNALPOST_PAPIRSØKNAD)
        assertThat(slot.captured.metadata["hendelsesType"]).isEqualTo("UgyldigType")
    }

    @Test
    fun `Hendelser hvor journalpost ikke har tema for Barnetrygd skal ignoreres`() {
        val ukjentTemaRecord = opprettRecord(journalpostId = JOURNALPOST_PAPIRSØKNAD, temaNytt = "UKJ")

        val consumerRecord = ConsumerRecord("topic", 1,
                                            OFFSET,
                                            42L, ukjentTemaRecord)

        service.prosesserNyHendelse(consumerRecord, ack)

        verify { ack.acknowledge() }
        val slot = slot<Hendelseslogg>()
        verify(exactly = 1) {
            mockHendelsesloggRepository.save(capture(slot))
        }
        assertThat(slot.captured).isNotNull
        assertThat(slot.captured.offset).isEqualTo(OFFSET)
        assertThat(slot.captured.hendelseId).isEqualTo(HENDELSE_ID)
        assertThat(slot.captured.metadata["journalpostId"]).isEqualTo(JOURNALPOST_PAPIRSØKNAD)
        assertThat(slot.captured.metadata["hendelsesType"]).isEqualTo("MidlertidigJournalført")
    }

    private fun opprettRecord(journalpostId: String,
                              hendelseType: String = "MidlertidigJournalført",
                              temaNytt: String = "ENF"): JournalfoeringHendelseRecord {
        return JournalfoeringHendelseRecord(HENDELSE_ID,
                                            1,
                                            hendelseType,
                                            journalpostId.toLong(),
                                            "M",
                                            "ENF",
                                            temaNytt,
                                            "SKAN_NETS",
                                            "kanalReferanseId",
                                            "ENF")
    }

    companion object {

        const val JOURNALPOST_PAPIRSØKNAD = "111"
        const val JOURNALPOST_DIGITALSØKNAD = "222"
        const val JOURNALPOST_UTGÅENDE_DOKUMENT = "333"
        const val JOURNALPOST_IKKE_ENFNETRYGD = "444"
        const val JOURNALPOST_FERDIGSTILT = "555"
        const val OFFSET = 21L
        const val HENDELSE_ID = "hendelseId"
    }
}
