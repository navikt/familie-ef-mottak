package no.nav.familie.ef.mottak.task

import io.mockk.MockKMatcherScope
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.AutomatiskJournalføringService
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.kontrakter.ef.journalføring.AutomatiskJournalføringResponse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import no.nav.familie.util.FnrGenerator
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Properties
import java.util.UUID

internal class AutomatiskJournalførTaskTest {

    val automatiskJournalføringService: AutomatiskJournalføringService = mockk()
    val søknadService: SøknadService = mockk()
    val taskRepository: TaskRepository = mockk()

    val automatiskJournalførTask = AutomatiskJournalførTask(
        taskRepository = taskRepository,
        automatiskJournalføringService = automatiskJournalføringService,
        søknadService = søknadService
    )

    private val task: Task
        get() {
            val task =
                Task(type = AutomatiskJournalførTask.TYPE, payload = overgangsstønadSøknadId, properties = Properties())
            task.metadata.apply {
                this["journalpostId"] = "123"
            }
            return task
        }

    private val overgangsstønadSøknadId = "123L"

    @BeforeEach
    internal fun setUp() {
        every {
            søknadService.get(overgangsstønadSøknadId)
        } returns Søknad(
            søknadJson = EncryptedString(""),
            dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
            journalpostId = "1234",
            fnr = FnrGenerator.generer()
        )
    }

    @Test
    internal fun `Skal bruke fallback manuell journalføring dersom vi får exception når vi kaller på ef-sak`() {
        val taskSlot = slot<Task>()
        every { taskRepository.save(capture(taskSlot)) } answers { taskSlot.captured }
        every { automatiskJournalføring() } returns false

        automatiskJournalførTask.doTask(task)

        Assertions.assertThat(taskSlot.captured.type).isEqualTo(manuellJournalføringFlyt().first().type)
    }

    @Test
    internal fun `Skal ikke bruke fallback til manuell journalføring når alt er ok`() {
        val response = AutomatiskJournalføringResponse(
            fagsakId = UUID.randomUUID(),
            behandlingId = UUID.randomUUID(),
            behandleSakOppgaveId = 0
        )
        every { automatiskJournalføring() } returns true
        automatiskJournalførTask.doTask(task)
        verify(exactly = 0) { taskRepository.save(any()) }
    }

    private fun MockKMatcherScope.automatiskJournalføring() =
        automatiskJournalføringService.journalførAutomatisk(
            any(),
            any(),
            any()
        )
}
