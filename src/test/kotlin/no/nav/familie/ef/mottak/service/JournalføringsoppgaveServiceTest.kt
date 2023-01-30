package no.nav.familie.ef.mottak.service

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.hendelse.JournalfoeringHendelseDbUtil
import no.nav.familie.ef.mottak.mockapi.clearAllMocks
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.HendelsesloggRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.TaskRepositoryUtvidet
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.journalpost.Bruker
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Properties

class JournalføringsoppgaveServiceTest {

    lateinit var service: JournalføringsoppgaveService

    @MockK
    lateinit var mockTaskRepositoryUtvidet: TaskRepositoryUtvidet

    @MockK
    lateinit var mockTaskService: TaskService

    @MockK(relaxed = true)
    lateinit var mockSøknadRepository: SøknadRepository

    @MockK(relaxed = true)
    lateinit var mockHendelseloggRepository: HendelsesloggRepository

    private lateinit var mockJournalfoeringHendelseDbUtil: JournalfoeringHendelseDbUtil

    @MockK(relaxed = true)
    lateinit var mockEttersendingRepository: EttersendingRepository

    @MockK
    lateinit var journalpost: Journalpost

    @BeforeEach
    internal fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks(this)

        every { journalpost.tema } returns "ENF"
        every { journalpost.journalposttype } returns Journalposttype.I
        every { journalpost.journalpostId } returns "1"

        mockJournalfoeringHendelseDbUtil =
            JournalfoeringHendelseDbUtil(mockHendelseloggRepository, mockTaskService, mockTaskRepositoryUtvidet)

        service = JournalføringsoppgaveService(
            mockSøknadRepository,
            mockEttersendingRepository,
            mockJournalfoeringHendelseDbUtil,
        )
    }

    @Test
    fun `Hendelser hvor journalpost er alt FERDIGSTILT skal ignoreres`() {
        every { journalpost.journalstatus } returns Journalstatus.FERDIGSTILT

        service.lagEksternJournalføringTask(journalpost)

        verify(exactly = 0) {
            mockTaskService.save(any())
        }
    }

    @Test
    fun `Utgående journalposter skal ignoreres`() {
        every { journalpost.journalstatus } returns Journalstatus.UTGAAR

        service.lagEksternJournalføringTask(journalpost)

        verify(exactly = 0) {
            mockTaskService.save(any())
        }
    }

    @Test
    fun `Journalposter som har en søknad skal ikke opprette task`() {
        every { journalpost.journalstatus } returns Journalstatus.MOTTATT
        every { journalpost.kanal } returns "NAV_NO"
        every { mockSøknadRepository.findByJournalpostId(any()) } returns søknad()

        service.lagEksternJournalføringTask(journalpost)

        verify(exactly = 0) {
            mockTaskService.save(any())
        }
    }

    @Test
    fun `Journalposter som har en ettersending skal ikke opprette task`() {
        every { journalpost.journalstatus } returns Journalstatus.MOTTATT
        every { journalpost.kanal } returns "NAV_NO"
        every { mockEttersendingRepository.findByJournalpostId(any()) } returns
            Ettersending(
                ettersendingJson = EncryptedString(""),
                stønadType = "OVERGANGSSTØNAD",
                fnr = "",
            )

        service.lagEksternJournalføringTask(journalpost)

        verify(exactly = 0) {
            mockTaskService.save(any())
        }
    }

    @Test
    fun `Journalposter som er mottatt uten ettersending eller søknad skal opprettes`() {
        journalpost = Journalpost(
            "123",
            Journalposttype.I,
            Journalstatus.MOTTATT,
            tema = "ENF",
            behandlingstema = "abc123",
            bruker = Bruker("213", BrukerIdType.FNR),
            kanal = "NAV_NO",
        )
        every { mockEttersendingRepository.findByJournalpostId(any()) } returns null
        every { mockSøknadRepository.findByJournalpostId(any()) } returns null
        every { mockTaskService.save(any()) } returns Task("", "", Properties())

        service.lagEksternJournalføringTask(journalpost)

        verify(exactly = 1) {
            mockTaskService.save(any())
        }
    }
}
