package no.nav.familie.ef.mottak.hendelse

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.mockapi.clearAllMocks
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.JournalføringHendelseRecordVars.JOURNALPOST_DIGITALSØKNAD
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.JournalføringHendelseRecordVars.JOURNALPOST_FERDIGSTILT
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.JournalføringHendelseRecordVars.JOURNALPOST_IKKE_ENFNETRYGD
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.JournalføringHendelseRecordVars.JOURNALPOST_PAPIRSØKNAD
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.JournalføringHendelseRecordVars.JOURNALPOST_UTGÅENDE_DOKUMENT
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.JournalføringHendelseRecordVars.OFFSET
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.journalføringHendelseRecord
import no.nav.familie.ef.mottak.repository.HendelsesloggRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.TaskRepositoryUtvidet
import no.nav.familie.ef.mottak.repository.domain.Hendelseslogg
import no.nav.familie.ef.mottak.service.JournalføringsoppgaveService
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.journalpost.Bruker
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
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

    @MockK
    lateinit var mockTaskService: TaskService

    @MockK(relaxed = true)
    lateinit var mockHendelseloggRepository: HendelsesloggRepository

    @MockK(relaxed = true)
    lateinit var mockSøknadRepository: SøknadRepository

    @MockK(relaxed = true)
    lateinit var mockJournalføringsoppgaveService: JournalføringsoppgaveService

    private lateinit var mockJournalfoeringHendelseDbUtil: JournalfoeringHendelseDbUtil

    lateinit var service: JournalhendelseService

    @BeforeEach
    internal fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks(this)

        every {
            mockHendelseloggRepository.insert(any())
        } returns Hendelseslogg(offset = 1L, hendelseId = "")

        every {
            mockTaskService.save(any())
        } answers { it.invocation.args[0] as Task }

        // Inngående papirsøknad, Mottatt
        every {
            integrasjonerClient.hentJournalpost(JOURNALPOST_PAPIRSØKNAD)
        } returns
            Journalpost(
                journalpostId = JOURNALPOST_PAPIRSØKNAD,
                journalposttype = Journalposttype.I,
                journalstatus = Journalstatus.MOTTATT,
                bruker = Bruker("123456789012", BrukerIdType.AKTOERID),
                tema = "ENF",
                kanal = "SKAN_NETS",
                behandlingstema = null,
                dokumenter = null,
                journalforendeEnhet = null,
                sak = null,
            )

        // Inngående digital, Mottatt
        every {
            integrasjonerClient.hentJournalpost(JOURNALPOST_DIGITALSØKNAD)
        } returns
            Journalpost(
                journalpostId = JOURNALPOST_DIGITALSØKNAD,
                journalposttype = Journalposttype.I,
                journalstatus = Journalstatus.MOTTATT,
                bruker = Bruker("123456789012", BrukerIdType.AKTOERID),
                tema = "ENF",
                kanal = "NAV_NO",
            )

        // Utgående digital, Mottatt
        every {
            integrasjonerClient.hentJournalpost(JOURNALPOST_UTGÅENDE_DOKUMENT)
        } returns
            Journalpost(
                journalpostId = JOURNALPOST_UTGÅENDE_DOKUMENT,
                journalposttype = Journalposttype.U,
                journalstatus = Journalstatus.MOTTATT,
                bruker = Bruker("123456789012", BrukerIdType.AKTOERID),
                tema = "ENF",
                kanal = "SKAN_NETS",
            )

        // Ikke barnetrygd
        every {
            integrasjonerClient.hentJournalpost(JOURNALPOST_IKKE_ENFNETRYGD)
        } returns
            Journalpost(
                journalpostId = JOURNALPOST_IKKE_ENFNETRYGD,
                journalposttype = Journalposttype.U,
                journalstatus = Journalstatus.MOTTATT,
                bruker = Bruker("123456789012", BrukerIdType.AKTOERID),
                tema = "FOR",
                kanal = "NAV_NO",
            )

        // ferdigstilt journalpost
        every {
            integrasjonerClient.hentJournalpost(JOURNALPOST_FERDIGSTILT)
        } returns
            Journalpost(
                journalpostId = JOURNALPOST_FERDIGSTILT,
                journalposttype = Journalposttype.U,
                journalstatus = Journalstatus.FERDIGSTILT,
                bruker = Bruker("123456789012", BrukerIdType.AKTOERID),
                tema = "FOR",
                kanal = "NAV_NO",
            )

        mockJournalfoeringHendelseDbUtil =
            JournalfoeringHendelseDbUtil(mockHendelseloggRepository, mockTaskService, mockTaskRepositoryUtvidet)

        service =
            JournalhendelseService(
                integrasjonerClient,
                mockSøknadRepository,
                mockJournalfoeringHendelseDbUtil,
                mockJournalføringsoppgaveService,
                mockTaskRepositoryUtvidet,
            )
    }

    @Test
    fun `Mottak av papirsøknader skal opprette LagEksternJournalføringsoppgaveTask`() {
        MDC.put("callId", "papir")
        val journalføringhendelseRecord = journalføringHendelseRecord(JOURNALPOST_PAPIRSØKNAD)

        every { mockSøknadRepository.findByJournalpostId(any()) } returns null

        every {
            mockTaskRepositoryUtvidet.existsByPayloadAndType(any(), any())
        } returns false

        service.prosesserNyHendelse(journalføringhendelseRecord, OFFSET)

        val taskSlot = slot<Journalpost>()
        verify {
            mockJournalføringsoppgaveService.lagEksternJournalføringTask(capture(taskSlot))
        }

        assertThat(taskSlot.captured).isNotNull
        assertThat(taskSlot.captured.kanal).isEqualTo(journalføringhendelseRecord.mottaksKanal)
    }

    @Test
    fun `Mottak av papirsøknader skal ikke opprette LagEksternJournalføringsoppgaveTask hvis det allerede finnes en task med samme journalpostId`() {
        MDC.put("callId", "papir")
        val record = journalføringHendelseRecord(JOURNALPOST_PAPIRSØKNAD)

        every { mockSøknadRepository.findByJournalpostId(any()) } returns null

        every {
            mockTaskRepositoryUtvidet.existsByPayloadAndType(any(), any())
        } returns true

        service.prosesserNyHendelse(record, OFFSET)

        verify(exactly = 0) {
            mockTaskService.save(any())
        }

        verify(exactly = 1) {
            mockHendelseloggRepository.insert(any())
        }
    }

    @Test
    fun `Skal ignorere hendelse fordi hendelseId eksisterer i hendelseslogg-tabellen i db`() {
        val journalføringhendelseRecord = journalføringHendelseRecord(JOURNALPOST_PAPIRSØKNAD)
        every { mockHendelseloggRepository.existsByHendelseId(any()) } returns true

        service.prosesserNyHendelse(journalføringhendelseRecord, OFFSET)

        verify(exactly = 0) {
            mockHendelseloggRepository.insert(any())
        }
    }

    @Test
    fun `Mottak av gyldig hendelse skal delegeres til service`() {
        every { mockHendelseloggRepository.existsByHendelseId(any()) } returns false

        every {
            mockTaskRepositoryUtvidet.existsByPayloadAndType(any(), any())
        } returns false
        val journalføringHendelseRecord = journalføringHendelseRecord(JOURNALPOST_PAPIRSØKNAD)
        service.prosesserNyHendelse(journalføringHendelseRecord, OFFSET)

        val slot = slot<Hendelseslogg>()
        verify(exactly = 1) {
            mockHendelseloggRepository.insert(capture(slot))
        }

        assertThat(slot.captured).isNotNull
        assertThat(slot.captured.offset).isEqualTo(OFFSET)
        assertThat(slot.captured.hendelseId).isEqualTo(journalføringHendelseRecord.hendelsesId)
        assertThat(slot.captured.metadata["journalpostId"]).isEqualTo(JOURNALPOST_PAPIRSØKNAD)
        assertThat(slot.captured.metadata["hendelsesType"]).isEqualTo("JournalpostMottatt")
    }
}
