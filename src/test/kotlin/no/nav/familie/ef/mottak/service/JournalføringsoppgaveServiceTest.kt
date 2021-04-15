package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.service

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.hendelse.JournalfoeringHendelseDbUtil
import no.nav.familie.ef.mottak.repository.HendelsesloggRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.TaskRepositoryUtvidet
import no.nav.familie.ef.mottak.service.JournalføringsoppgaveService
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JournalføringsoppgaveServiceTest {

    lateinit var service: JournalføringsoppgaveService

    @MockK
    lateinit var mockTaskRepositoryUtvidet: TaskRepositoryUtvidet

    @MockK(relaxed = true)
    lateinit var mockSøknadRepository: SøknadRepository

    @MockK(relaxed = true)
    lateinit var mockHendelseloggRepository: HendelsesloggRepository

    @MockK(relaxed = true)
    lateinit var mockFeatureToggleService: FeatureToggleService

    lateinit var mockJournalfoeringHendelseDbUtil: JournalfoeringHendelseDbUtil

    @MockK
    lateinit var journalpost: Journalpost

    @BeforeEach
    internal fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { mockFeatureToggleService.isEnabled(any()) } returns true

        every { journalpost.tema } returns "ENF"
        every { journalpost.journalposttype } returns Journalposttype.I
        every { journalpost.journalpostId } returns "1"

        mockJournalfoeringHendelseDbUtil = JournalfoeringHendelseDbUtil(mockHendelseloggRepository, mockTaskRepositoryUtvidet)

        service = JournalføringsoppgaveService(mockFeatureToggleService, mockSøknadRepository, mockJournalfoeringHendelseDbUtil)
    }

    @Test
    fun `Hendelser hvor journalpost er alt FERDIGSTILT skal ignoreres`() {

        every { journalpost.journalstatus } returns Journalstatus.FERDIGSTILT

        service.lagEksternJournalføringTask(journalpost)

        verify(exactly = 0) {
            mockTaskRepositoryUtvidet.save(any())
        }

    }

    @Test
    fun `Utgående journalposter skal ignoreres`() {

        every { journalpost.journalstatus } returns Journalstatus.UTGAAR

        service.lagEksternJournalføringTask(journalpost)

        verify(exactly = 0) {
            mockTaskRepositoryUtvidet.save(any())
        }

    }
}