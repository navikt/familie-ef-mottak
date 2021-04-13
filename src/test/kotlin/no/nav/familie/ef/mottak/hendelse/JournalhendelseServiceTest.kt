package no.nav.familie.ef.mottak.hendelse

import io.mockk.*
import io.mockk.impl.annotations.MockK
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.HendelsesloggRepository
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.TaskRepositoryUtvidet
import no.nav.familie.ef.mottak.repository.domain.Hendelseslogg
import no.nav.familie.ef.mottak.service.JournalføringsoppgaveService
import no.nav.familie.kontrakter.felles.journalpost.*
import no.nav.familie.prosessering.domene.Task
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.MDC

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JournalhendelseServiceTest {

    @MockK
    lateinit var integrasjonerClient: IntegrasjonerClient

    @MockK
    lateinit var mockTaskRepositoryUtvidet: TaskRepositoryUtvidet

    @MockK(relaxed = true)
    lateinit var mockFeatureToggleService: FeatureToggleService

    @MockK(relaxed = true)
    lateinit var mockHendelseloggRepository: HendelsesloggRepository

    @MockK(relaxed = true)
    lateinit var mockSøknadRepository: SoknadRepository

    @MockK(relaxed = true)
    lateinit var mockJournalføringsoppgaveService: JournalføringsoppgaveService

    @MockK(relaxed = true)
    lateinit var mockJournalfoeringHendelseDbUtil: JournalfoeringHendelseDbUtil

    lateinit var service: JournalhendelseService

    @BeforeEach
    internal fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every {
            mockHendelseloggRepository.save(any())
        } returns Hendelseslogg(offset = 1L, hendelseId = "")

        every {
            mockTaskRepositoryUtvidet.save(any())
        } answers { it.invocation.args[0] as Task }

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

        mockJournalfoeringHendelseDbUtil = JournalfoeringHendelseDbUtil(mockHendelseloggRepository, mockTaskRepositoryUtvidet)

        service = JournalhendelseService(integrasjonerClient, mockFeatureToggleService, mockSøknadRepository, mockJournalfoeringHendelseDbUtil, mockJournalføringsoppgaveService, mockTaskRepositoryUtvidet)
    }

    @Test
    fun `Mottak av papirsøknader skal opprette LagEksternJournalføringsoppgaveTask`() {
        MDC.put("callId", "papir")
        val record = opprettRecord(JOURNALPOST_PAPIRSØKNAD)

        every { mockSøknadRepository.findByJournalpostId(any()) } returns null

        every {
            mockTaskRepositoryUtvidet.existsByPayloadAndType(any(), any())
        } returns false

        service.prosesserNyHendelse(record, OFFSET)

        val taskSlot = slot<Journalpost>()
        verify {
            mockJournalføringsoppgaveService.lagEksternJournalføringTask(capture(taskSlot))
        }

        assertThat(taskSlot.captured).isNotNull
        assertThat(taskSlot.captured.kanal).isEqualTo(record.mottaksKanal)
    }

    @Test
    fun `Mottak av papirsøknader skal ikke opprette LagEksternJournalføringsoppgaveTask hvis det allerede finnes en task med samme journalpostId`() {
        MDC.put("callId", "papir")
        val record = opprettRecord(JOURNALPOST_PAPIRSØKNAD)

        every { mockSøknadRepository.findByJournalpostId(any()) } returns null

        every {
            mockTaskRepositoryUtvidet.existsByPayloadAndType(any(), any())
        } returns true

        service.prosesserNyHendelse(record, OFFSET)

        verify(exactly = 0) {
            mockTaskRepositoryUtvidet.save(any())
        }

        verify(exactly = 1) {
            mockHendelseloggRepository.save(any())
        }
    }

    @Test
    fun `Skal ignorere hendelse fordi den eksisterer i hendelseslogg`() {
        val consumerRecord = ConsumerRecord("topic", 1,
                                            OFFSET,
                                            42L, opprettRecord(JOURNALPOST_PAPIRSØKNAD))
        every {
            mockJournalfoeringHendelseDbUtil.erHendelseRegistrertIHendelseslogg(consumerRecord.value())
        } returns true

        service.prosesserNyHendelse(consumerRecord.value(), consumerRecord.offset())

        verify(exactly = 0) {
            mockHendelseloggRepository.save(any())
        }
    }

    @Test
    fun `Mottak av gyldig hendelse skal delegeres til service`() {
        val consumerRecord = ConsumerRecord("topic", 1,
                                            OFFSET,
                                            42L, opprettRecord(JOURNALPOST_PAPIRSØKNAD))

        every {
            mockJournalfoeringHendelseDbUtil.erHendelseRegistrertIHendelseslogg(consumerRecord.value())
        } returns false

        every {
            mockTaskRepositoryUtvidet.existsByPayloadAndType(any(), any())
        } returns false

        service.prosesserNyHendelse(consumerRecord.value(),
                                    consumerRecord.offset())

        val slot = slot<Hendelseslogg>()
        verify(exactly = 1) {
            mockHendelseloggRepository.save(capture(slot))
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
